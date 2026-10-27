param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$GradleHome = (Join-Path $env:USERPROFILE '.gradle'),
    [string]$AssetsDir = (Join-Path $env:APPDATA 'ModrinthApp/meta/assets'),
    [string]$AssetIndex = '29'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot
Push-Location $projectRoot
try {
    $cache = Join-Path $GradleHome 'caches/modules-2/files-2.1'
    $mc = (Get-Content src/main/resources/fabric.mod.json -Raw | ConvertFrom-Json).depends.minecraft
    $gradle = Get-Content build.gradle -Raw
    $api = [regex]::Match($gradle, "fabric-api:([^':]+)'").Groups[1].Value
    $menu = [regex]::Match($gradle, "modmenu:([^']+)").Groups[1].Value
    function Find-Jar([string]$Coordinate) {
        $parts = $Coordinate.Split(':')
        $directory = Join-Path $cache ($parts[0] + '/' + $parts[1] + '/' + $parts[2])
        $name = $parts[1] + '-' + $parts[2]
        if ($parts.Length -gt 3) { $name += '-' + $parts[3] }
        $file = Get-ChildItem $directory -Recurse -Filter ($name + '.jar') -ErrorAction SilentlyContinue | Select-Object -First 1
        if (-not $file) { throw "Missing library $Coordinate" }
        return $file.FullName
    }
    $runDir = Join-Path $projectRoot 'build/x1eqn-smoke/run'
    New-Item -ItemType Directory -Force build/x1eqn-smoke/classes,build/x1eqn-smoke/resources,(Join-Path $runDir 'mods') | Out-Null
    $testArgs = [IO.File]::ReadAllLines((Join-Path $projectRoot 'build/x1eqn/test-javac.args'))
    $compilerArgs = @('--release','25','-encoding','UTF-8','-proc:none','-cp',$testArgs[6],'-d','build/x1eqn-smoke/classes','tests/dev/x1eqn/skyshaders/qa/ClientSmoke.java')
    [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn-smoke/compile.args'),$compilerArgs)
    & (Join-Path $JavaHome 'bin/javac.exe') '@build/x1eqn-smoke/compile.args'
    if ($LASTEXITCODE) { throw 'QA compilation failed' }
    $qa = @{schemaVersion=1; id='x1eqnsky_smoke'; version='1.0'; environment='client'; entrypoints=@{client=@('dev.x1eqn.skyshaders.qa.ClientSmoke')}; depends=@{x1eqn_sky_shaders='*'; modmenu='*'}}
    [IO.File]::WriteAllText((Join-Path $projectRoot 'build/x1eqn-smoke/resources/fabric.mod.json'),($qa | ConvertTo-Json -Depth 6))
    & (Join-Path $JavaHome 'bin/jar.exe') --create --file build/x1eqn-smoke/run/mods/qa.jar -C build/x1eqn-smoke/classes . -C build/x1eqn-smoke/resources .
    Copy-Item ("build/libs/sky-shaders-1.1.0+"+$mc+'.jar'),(Find-Jar "net.fabricmc.fabric-api:fabric-api:$api"),(Find-Jar "com.terraformersmc:modmenu:$menu") (Join-Path $runDir 'mods')
    $info = Get-Content (Join-Path $GradleHome "caches/fabric-loom/$mc/mojang_minecraft_info.json") -Raw | ConvertFrom-Json
    $libraries = @()
    foreach ($library in $info.libraries) {
        if ($library.name -match '^org.ow2.asm:|^ca.weblite:') { continue }
        if ($library.name -match ':natives-' -and $library.name -notmatch ':natives-windows$') { continue }
        $libraries += Find-Jar $library.name
    }
    $libraries += Find-Jar 'net.fabricmc:fabric-loader:0.19.5'
    foreach ($artifact in @('asm','asm-analysis','asm-commons','asm-tree','asm-util')) { $libraries += Find-Jar "org.ow2.asm:${artifact}:9.10.1" }
    $mixin = Get-ChildItem (Join-Path $cache 'net.fabricmc/sponge-mixin') -Directory | Sort-Object Name -Descending | Select-Object -First 1
    $libraries += @(Get-ChildItem $mixin.FullName -Recurse -Filter '*.jar' | Where-Object {$_.Name -notmatch '(sources|javadoc)'} | ForEach-Object FullName)
    $libraries += Join-Path $GradleHome "caches/fabric-loom/$mc/minecraft-client.jar"
    [IO.File]::WriteAllText((Join-Path $runDir 'options.txt'), 'version:4903'+[Environment]::NewLine+'preferredGraphicsBackend:"opengl"'+[Environment]::NewLine)
    $launch = @('-Xmx2G','-Dfabric.log.level=info','-cp',('"'+($libraries -join ';').Replace('\','/')+'"'),'net.fabricmc.loader.impl.launch.knot.KnotClient','--username','SkyShaderTest','--version',$mc,'--accessToken','0','--gameDir','.', '--assetsDir',('"'+$AssetsDir.Replace('\','/')+'"'),'--assetIndex',$AssetIndex,'--width','320','--height','240')
    [IO.File]::WriteAllLines((Join-Path $runDir 'launch.args'),$launch)
    Push-Location $runDir
    try {
        $testProcess = Start-Process -FilePath (Join-Path $JavaHome 'bin/java.exe') -ArgumentList '@launch.args' -WindowStyle Hidden -PassThru -RedirectStandardOutput stdout.log -RedirectStandardError stderr.log
        $testProcess.Id | Set-Content test.pid
        Write-Output "Smoke client started: PID $($testProcess.Id). Results: build/x1eqn-smoke/run/smoke-report.txt"
    } finally { Pop-Location }
} finally { Pop-Location }
