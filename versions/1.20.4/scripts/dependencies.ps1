$cache = Join-Path $GradleHome 'caches/modules-2/files-2.1'
$depsRoot = Join-Path $projectRoot 'build/deps'
New-Item -ItemType Directory -Force $depsRoot | Out-Null

function Find-Jar([string]$Coordinate, [string]$DownloadUrl = '') {
    $parts = $Coordinate.Split(':')
    $name = $parts[1] + '-' + $parts[2]
    if ($parts.Length -gt 3) { $name += '-' + $parts[3] }
    $name += '.jar'
    $directory = Join-Path $cache ($parts[0]+'/'+$parts[1]+'/'+$parts[2])
    $found = Get-ChildItem $directory -Recurse -Filter $name -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { return $found.FullName }
    $target = Join-Path $depsRoot $name
    if (-not (Test-Path $target)) {
        if (-not $DownloadUrl) {
            $DownloadUrl = 'https://libraries.minecraft.net/'+$parts[0].Replace('.','/')+'/'+$parts[1]+'/'+$parts[2]+'/'+$name
        }
        Invoke-WebRequest -Uri $DownloadUrl -OutFile $target
    }
    return $target
}

function Download-Checked([string]$Url, [string]$Target, [string]$Sha1 = '') {
    if (-not (Test-Path $Target)) { Invoke-WebRequest -Uri $Url -OutFile $Target }
    if ($Sha1 -and (Get-FileHash $Target -Algorithm SHA1).Hash -ne $Sha1.ToUpper()) { throw "Checksum mismatch: $Target" }
}

$metadata = Join-Path $projectRoot 'build/mojang_minecraft_info.json'
if (-not (Test-Path $metadata)) {
    $manifest = Invoke-RestMethod 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
    $release = $manifest.versions | Where-Object id -eq '1.20.4' | Select-Object -First 1
    Download-Checked $release.url $metadata
}
$info = Get-Content $metadata -Raw | ConvertFrom-Json
Download-Checked $info.downloads.client.url (Join-Path $projectRoot 'build/minecraft-client.jar') $info.downloads.client.sha1
Download-Checked $info.downloads.client_mappings.url (Join-Path $projectRoot 'build/client-mappings.txt') $info.downloads.client_mappings.sha1
Download-Checked 'https://maven.fabricmc.net/net/fabricmc/intermediary/1.20.4/intermediary-1.20.4-v2.jar' (Join-Path $depsRoot 'intermediary-1.20.4-v2.jar')
Download-Checked 'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.97.3+1.20.4/fabric-api-0.97.3+1.20.4.jar' (Join-Path $depsRoot 'fabric-api.jar')
Download-Checked 'https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/9.2.0/modmenu-9.2.0.jar' (Join-Path $depsRoot 'modmenu.jar')

$runtimeLibraries = @()
foreach ($library in $info.libraries) {
    if ($library.name -match '^ca.weblite:|^org.ow2.asm:') { continue }
    if ($library.name -match ':natives-' -and $library.name -notmatch ':natives-windows$') { continue }
    $runtimeLibraries += Find-Jar $library.name $library.downloads.artifact.url
}
$runtimeLibraries += Find-Jar 'net.fabricmc:fabric-loader:0.19.5'
foreach ($artifact in @('asm','asm-analysis','asm-commons','asm-tree','asm-util')) {
    $runtimeLibraries += Find-Jar "org.ow2.asm:${artifact}:9.10.1"
}
$mixin = Get-ChildItem (Join-Path $cache 'net.fabricmc/sponge-mixin') -Directory | Sort-Object Name -Descending | Select-Object -First 1
$runtimeLibraries += @(Get-ChildItem $mixin.FullName -Filter '*.jar' -Recurse | Where-Object {$_.Name -notmatch '(sources|javadoc)'} | ForEach-Object FullName)
$runtimeLibraries += Join-Path $depsRoot 'intermediary-1.20.4-v2.jar'
$runtimeLibraries | Set-Content (Join-Path $projectRoot 'build/runtime-libraries.txt')

Add-Type -AssemblyName System.IO.Compression.FileSystem
$intermediaryZip = [IO.Compression.ZipFile]::OpenRead((Join-Path $depsRoot 'intermediary-1.20.4-v2.jar'))
try { [IO.Compression.ZipFileExtensions]::ExtractToFile($intermediaryZip.GetEntry('mappings/mappings.tiny'), (Join-Path $projectRoot 'build/intermediary-v2.tiny'), $true) }
finally { $intermediaryZip.Dispose() }
$apiZip = [IO.Compression.ZipFile]::OpenRead((Join-Path $depsRoot 'fabric-api.jar'))
try {
    foreach ($name in @('fabric-api-base','fabric-lifecycle-events-v1')) {
        $entry = $apiZip.Entries | Where-Object {$_.Name -like "$name-*.jar"} | Select-Object -First 1
        if (-not $entry) { throw "Missing nested Fabric module: $name" }
        [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, (Join-Path $depsRoot ($name+'.jar')), $true)
    }
} finally { $apiZip.Dispose() }
