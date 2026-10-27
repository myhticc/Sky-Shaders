param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$GradleHome = (Join-Path $env:USERPROFILE '.gradle'),
    [switch]$ValidateGpu
)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome 'bin/javac.exe'))) {
    throw 'Pass -JavaHome with a JDK 25 directory, or set JAVA_HOME.'
}
$projectRoot = Split-Path $PSScriptRoot
Push-Location $projectRoot
try {
    $cache = Join-Path $GradleHome 'caches/modules-2/files-2.1'
    function Find-Jar([string]$Coordinate) {
        $parts = $Coordinate.Split(':')
        $directory = Join-Path $cache ($parts[0] + '/' + $parts[1] + '/' + $parts[2])
        $name = $parts[1] + '-' + $parts[2]
        if ($parts.Length -gt 3) { $name += '-' + $parts[3] }
        $match = Get-ChildItem $directory -Recurse -Filter ($name + '.jar') -ErrorAction SilentlyContinue | Select-Object -First 1
        if (-not $match) { throw "Missing cached dependency: $Coordinate. Run gradlew.bat build online first." }
        return $match.FullName
    }
    $minecraft = Join-Path $GradleHome 'caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.3/minecraft-merged-deobf-26.3.jar'
    $metadata = Join-Path $GradleHome 'caches/fabric-loom/26.3/mojang_minecraft_info.json'
    if (-not (Test-Path $minecraft) -or -not (Test-Path $metadata)) { throw 'Minecraft 26.3 Loom cache is required. Run gradlew.bat build online first.' }
    $jars = [System.Collections.Generic.List[string]]::new()
    $jars.Add($minecraft)
    $info = Get-Content $metadata -Raw | ConvertFrom-Json
    foreach ($library in $info.libraries) {
        if ($library.name -notmatch '^(com\.google\.|com\.mojang:|org\.joml:|org\.slf4j:|org\.lwjgl:lwjgl(?:-sdl|-opengl)?:)') { continue }
        if ($library.name -match ':natives-' -and $library.name -notmatch ':natives-windows$') { continue }
        $jars.Add((Find-Jar $library.name))
    }
    $jars.Add((Find-Jar 'net.fabricmc:fabric-loader:0.19.5'))
    $jars.Add((Find-Jar 'com.terraformersmc:modmenu:21.0.0'))
    foreach ($dependency in @('fabric-api-base', 'fabric-lifecycle-events-v1')) {
        $apiPom = Get-ChildItem (Join-Path $cache 'net.fabricmc.fabric-api/fabric-api/0.164.0+26.3') -Filter '*.pom' -Recurse | Select-Object -First 1
        [xml]$pom = Get-Content $apiPom.FullName
        $item = $pom.project.dependencies.dependency | Where-Object { $_.artifactId -eq $dependency }
        $version = [string]$item.version
        if ($item.version.InnerText) { $version = $item.version.InnerText }
        $jars.Add((Find-Jar ('net.fabricmc.fabric-api:' + $dependency + ':' + $version)))
    }
    # Fabric Loader's Mixin/ASM jars are separate compile dependencies.
    foreach ($group in @('net.fabricmc/sponge-mixin', 'org.ow2.asm')) {
        Get-ChildItem (Join-Path $cache $group) -Recurse -Filter '*.jar' |
            Where-Object { $_.Name -notmatch '(sources|javadoc)' } | ForEach-Object { $jars.Add($_.FullName) }
    }
    $classpath = ($jars | Select-Object -Unique) -join ';'
    $classes = 'build/x1eqn/classes'
    New-Item -ItemType Directory -Force $classes, 'build/x1eqn/resources', 'build/libs' | Out-Null
    $compilerArgs = @('--release', '25', '-encoding', 'UTF-8', '-proc:none', '-classpath', ('"' + $classpath.Replace('\', '/') + '"'), '-d', $classes)
    $compilerArgs += @(Get-ChildItem src/main/java -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
    [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn/javac.args'), $compilerArgs)
    & (Join-Path $JavaHome 'bin/javac.exe') '@build/x1eqn/javac.args'
    if ($LASTEXITCODE) { throw 'Compilation failed' }
    $resourceRoot = Join-Path $projectRoot 'src/main/resources'
    foreach ($resource in Get-ChildItem $resourceRoot -File -Recurse) {
        $relative = [IO.Path]::GetRelativePath($resourceRoot, $resource.FullName)
        $destination = Join-Path $projectRoot ('build/x1eqn/resources/' + $relative)
        New-Item -ItemType Directory -Force (Split-Path $destination) | Out-Null
        Copy-Item -LiteralPath $resource.FullName -Destination $destination -Force
    }
    $manifest = [IO.File]::ReadAllText((Join-Path $projectRoot 'src/main/resources/fabric.mod.json')).Replace('${version}', '1.1.0+26.3')
    [IO.File]::WriteAllText((Join-Path $projectRoot 'build/x1eqn/resources/fabric.mod.json'), $manifest)
    & (Join-Path $JavaHome 'bin/jar.exe') --create --file 'build/libs/sky-shaders-1.1.0+26.3.jar' -C $classes . -C build/x1eqn/resources . LICENSE_x1eqn
    if ($LASTEXITCODE) { throw 'JAR packaging failed' }
    if ($ValidateGpu) {
        New-Item -ItemType Directory -Force build/x1eqn/tests | Out-Null
        $testClasspath = $classpath + ';' + (Join-Path $projectRoot $classes) + ';' + (Join-Path $projectRoot 'build/x1eqn/resources')
        $testArgs = @('--release', '25', '-encoding', 'UTF-8', '-proc:none', '-classpath', ('"' + $testClasspath.Replace('\', '/') + '"'), '-d', 'build/x1eqn/tests')
        $testArgs += @(Get-ChildItem tests -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
        [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn/test-javac.args'), $testArgs)
        & (Join-Path $JavaHome 'bin/javac.exe') '@build/x1eqn/test-javac.args'
        if ($LASTEXITCODE) { throw 'Validation compilation failed' }
        $runtimeArgs = @('-classpath', ('"' + ($testClasspath + ';' + (Join-Path $projectRoot 'build/x1eqn/tests')).Replace('\', '/') + '"'), 'dev.x1eqn.skyshaders.render.sky.Validation')
        [IO.File]::WriteAllLines((Join-Path $projectRoot 'build/x1eqn/test-java.args'), $runtimeArgs)
        & (Join-Path $JavaHome 'bin/java.exe') '@build/x1eqn/test-java.args'
        if ($LASTEXITCODE) { throw 'Validation failed' }
    }
    Write-Output 'Built build/libs/sky-shaders-1.1.0+26.3.jar'
} finally { Pop-Location }
