param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$GradleHome = (Join-Path $env:USERPROFILE '.gradle'),
    [switch]$ValidateGpu
)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { throw 'Set JAVA_HOME or pass -JavaHome with JDK 21; the mod targets Java 17.' }
$projectRoot = Split-Path $PSScriptRoot
Push-Location $projectRoot
try {
    New-Item -ItemType Directory -Force build/tools,build/x1eqn/classes,build/x1eqn/resources,build/libs | Out-Null
    . (Join-Path $PSScriptRoot 'dependencies.ps1')
    $toolJars = @(Get-ChildItem (Join-Path $cache 'net.fabricmc/tiny-remapper/0.14.0'),(Join-Path $cache 'net.fabricmc/mapping-io/0.8.0'),(Join-Path $cache 'org.ow2.asm') -Recurse -Filter '*.jar' | Where-Object {$_.Name -notmatch '(sources|javadoc)'} | ForEach-Object FullName)
    $toolCp = $toolJars -join ';'
    & (Join-Path $JavaHome 'bin/javac.exe') -cp $toolCp -d build/tools scripts/PrepareMappings.java scripts/Remap.java
    if ($LASTEXITCODE) { throw 'Tool compilation failed' }
    & (Join-Path $JavaHome 'bin/java.exe') -cp ($toolCp+';build/tools') PrepareMappings build/intermediary-v2.tiny build/client-mappings.txt build/mappings.tiny
    if ($LASTEXITCODE) { throw 'Mapping preparation failed' }
    function Invoke-Remap([string]$From,[string]$To,[string]$InputJar,[string]$OutputJar,[string[]]$Libraries) {
        $argsList = @('-Xmx2G','-cp',('"'+($toolCp+';'+(Join-Path $projectRoot 'build/tools')).Replace('\','/')+'"'),'Remap','build/mappings.tiny',$From,$To,('"'+$InputJar.Replace('\','/')+'"'),('"'+$OutputJar.Replace('\','/')+'"'))
        $argsList += @($Libraries | ForEach-Object {'"'+$_.Replace('\','/')+'"'})
        [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/remap.args'),$argsList)
        & (Join-Path $JavaHome 'bin/java.exe') '@build/remap.args'
        if ($LASTEXITCODE) { throw "Remapping failed: $InputJar" }
    }
    if (-not (Test-Path build/minecraft-mojmap.jar)) { Invoke-Remap official named 'build/minecraft-client.jar' 'build/minecraft-mojmap.jar' @() }
    if (-not (Test-Path build/minecraft-intermediary.jar)) { Invoke-Remap official intermediary 'build/minecraft-client.jar' 'build/minecraft-intermediary.jar' @() }
    foreach ($library in @('modmenu','fabric-api-base','fabric-lifecycle-events-v1')) {
        if (-not (Test-Path "build/$library-named.jar")) {
            Invoke-Remap intermediary named "build/deps/$library.jar" "build/$library-named.jar" @((Join-Path $projectRoot 'build/minecraft-intermediary.jar'))
        }
    }
    $classpath = (@((Join-Path $projectRoot 'build/minecraft-mojmap.jar'),(Join-Path $projectRoot 'build/modmenu-named.jar'),(Join-Path $projectRoot 'build/fabric-api-base-named.jar'),(Join-Path $projectRoot 'build/fabric-lifecycle-events-v1-named.jar')) + $runtimeLibraries + @($mixinExtrasJar)) -join ';'
    $compilerArgs = @('--release','17','-encoding','UTF-8','-proc:none','-classpath',('"'+$classpath.Replace('\','/')+'"'),'-d','build/x1eqn/classes')
    $compilerArgs += @(Get-ChildItem src/main/java -Recurse -Filter '*.java' | ForEach-Object {'"'+$_.FullName.Replace('\','/')+'"'})
    [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn/javac.args'),$compilerArgs)
    & (Join-Path $JavaHome 'bin/javac.exe') '@build/x1eqn/javac.args'
    if ($LASTEXITCODE) { throw 'Mod compilation failed' }
    $resourceRoot = Join-Path $projectRoot 'src/main/resources'
    foreach ($file in Get-ChildItem $resourceRoot -File -Recurse) {
        $relative = [IO.Path]::GetRelativePath($resourceRoot,$file.FullName)
        $target = Join-Path $projectRoot ('build/x1eqn/resources/'+$relative)
        New-Item -ItemType Directory -Force (Split-Path $target) | Out-Null
        Copy-Item -LiteralPath $file.FullName -Destination $target -Force
    }
    $manifest = [IO.File]::ReadAllText((Join-Path $resourceRoot 'fabric.mod.json')).Replace('${version}','1.1.2+1.20.1')
    [IO.File]::WriteAllText((Join-Path $projectRoot 'build/x1eqn/resources/fabric.mod.json'),$manifest)
    & (Join-Path $JavaHome 'bin/jar.exe') --create --file build/libs/sky-shaders-1.1.2+1.20.1-named.jar -C build/x1eqn/classes . -C build/x1eqn/resources . LICENSE_x1eqn
    if ($LASTEXITCODE) { throw 'Packaging failed' }
    if ($ValidateGpu) {
        New-Item -ItemType Directory -Force build/x1eqn/tests | Out-Null
        $testCp = $classpath+';'+(Join-Path $projectRoot 'build/x1eqn/classes')+';'+(Join-Path $projectRoot 'build/x1eqn/resources')
        $testArgs = @('--release','17','-encoding','UTF-8','-proc:none','-classpath',('"'+$testCp.Replace('\','/')+'"'),'-d','build/x1eqn/tests')
        $testArgs += @(Get-ChildItem tests -Recurse -Filter '*.java' | ForEach-Object {'"'+$_.FullName.Replace('\','/')+'"'})
        [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn/test-javac.args'),$testArgs)
        & (Join-Path $JavaHome 'bin/javac.exe') '@build/x1eqn/test-javac.args'
        if ($LASTEXITCODE) { throw 'Test compilation failed' }
        $runArgs = @('-cp',('"'+($testCp+';'+(Join-Path $projectRoot 'build/x1eqn/tests')).Replace('\','/')+'"'),'dev.x1eqn.skyshaders.render.sky.Validation')
        [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn/test-java.args'),$runArgs)
        & (Join-Path $JavaHome 'bin/java.exe') '@build/x1eqn/test-java.args'
        if ($LASTEXITCODE) { throw 'Validation failed' }
    }
    Invoke-Remap named intermediary 'build/libs/sky-shaders-1.1.2+1.20.1-named.jar' 'build/libs/sky-shaders-1.1.2+1.20.1.jar' (@((Join-Path $projectRoot 'build/minecraft-mojmap.jar'))+$runtimeLibraries+@($mixinExtrasJar))
    Write-Output 'Built build/libs/sky-shaders-1.1.2+1.20.1.jar (Java 17, Fabric intermediary)'
} finally { Pop-Location }
