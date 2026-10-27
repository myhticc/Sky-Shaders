param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$RuntimeJava = '',
    [string]$GradleHome = (Join-Path $env:USERPROFILE '.gradle'),
    [string]$AssetsDir = (Join-Path $env:APPDATA 'ModrinthApp/meta/assets'),
    [string]$AssetIndex = '29'
)
$ErrorActionPreference = 'Stop'
if (-not $RuntimeJava) { $RuntimeJava = Join-Path $JavaHome 'bin/java.exe' }
$projectRoot = Split-Path $PSScriptRoot
Push-Location $projectRoot
try {
    . (Join-Path $PSScriptRoot 'dependencies.ps1')
    $runDir = Join-Path $projectRoot 'build/x1eqn-smoke/run'
    New-Item -ItemType Directory -Force build/x1eqn-smoke/classes,build/x1eqn-smoke/resources,(Join-Path $runDir 'mods') | Out-Null
    $testArgs = [IO.File]::ReadAllLines((Join-Path $projectRoot 'build/x1eqn/test-javac.args'))
    $compile = @('--release','17','-encoding','UTF-8','-proc:none','-cp',$testArgs[6],'-d','build/x1eqn-smoke/classes','tests/dev/x1eqn/skyshaders/qa/ClientSmoke.java')
    [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn-smoke/compile.args'),$compile)
    & (Join-Path $JavaHome 'bin/javac.exe') '@build/x1eqn-smoke/compile.args'
    if ($LASTEXITCODE) { throw 'Smoke test compilation failed' }
    $qa = @{schemaVersion=1;id='x1eqnsky_smoke';version='1.0';environment='client';entrypoints=@{client=@('dev.x1eqn.skyshaders.qa.ClientSmoke')};depends=@{x1eqn_sky_shaders='*';modmenu='*'}}
    [IO.File]::WriteAllText((Join-Path $projectRoot 'build/x1eqn-smoke/resources/fabric.mod.json'),($qa | ConvertTo-Json -Depth 6))
    & (Join-Path $JavaHome 'bin/jar.exe') --create --file build/x1eqn-smoke/qa-named.jar -C build/x1eqn-smoke/classes . -C build/x1eqn-smoke/resources .
    $toolCp = @(Get-ChildItem (Join-Path $cache 'net.fabricmc/tiny-remapper/0.14.0'),(Join-Path $cache 'net.fabricmc/mapping-io/0.8.0'),(Join-Path $cache 'org.ow2.asm') -Recurse -Filter '*.jar' | Where-Object {$_.Name -notmatch '(sources|javadoc)'} | ForEach-Object FullName) -join ';'
    & (Join-Path $JavaHome 'bin/java.exe') -Xmx2G -cp ($toolCp+';build/tools') Remap build/mappings.tiny named intermediary build/x1eqn-smoke/qa-named.jar build/x1eqn-smoke/run/mods/qa.jar (Join-Path $projectRoot 'build/minecraft-mojmap.jar') (Join-Path $projectRoot 'build/libs/sky-shaders-1.1.2+1.20.1-named.jar')
    if ($LASTEXITCODE) { throw 'Smoke test remapping failed' }
    Copy-Item build/libs/sky-shaders-1.1.2+1.20.1.jar,build/deps/fabric-api.jar,build/deps/modmenu.jar (Join-Path $runDir 'mods') -Force
    $launchLibraries = $runtimeLibraries + (Join-Path $projectRoot 'build/minecraft-client.jar')
    $launch = @('-Xmx2G','-Dfabric.log.level=info','-cp',('"'+($launchLibraries -join ';').Replace('\','/')+'"'),'net.fabricmc.loader.impl.launch.knot.KnotClient','--username','SkyShaderTest','--version','1.20.1','--accessToken','0','--gameDir','.', '--assetsDir',('"'+$AssetsDir.Replace('\','/')+'"'),'--assetIndex',$AssetIndex,'--width','320','--height','240')
    [IO.File]::WriteAllLines((Join-Path $runDir 'launch.args'),$launch)
    Push-Location $runDir
    try {
        $process = Start-Process -FilePath $RuntimeJava -ArgumentList '@launch.args' -WindowStyle Hidden -PassThru -RedirectStandardOutput stdout.log -RedirectStandardError stderr.log
        $process.Id | Set-Content test.pid
        Write-Output "Minecraft 1.20.1 smoke test PID $($process.Id)"
    } finally { Pop-Location }
} finally { Pop-Location }
