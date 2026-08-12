$in  = "C:\Emulators\ROMS\PSP\Randomized\Star Ocean - First Departure (US)\PSP_GAME\USRDIR\so1pack.bin"
$out = "$env:USERPROFILE\Desktop\so1pack_head.bin"
$fs = [System.IO.File]::OpenRead($in)
$buffer = New-Object byte[] (8MB)
$read = $fs.Read($buffer, 0, $buffer.Length)
$fs.Close()
[System.IO.File]::WriteAllBytes($out, $buffer[0..($read-1)])