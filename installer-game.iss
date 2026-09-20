[Setup]
AppName=Dragon Tiger Game Link
AppVersion=1.0.0
DefaultDirName={autopf}\Dragon Tiger Game Link
DefaultGroupName=Dragon Tiger Game Link
OutputDir=installer
OutputBaseFilename=DragonTigerGameLink-v1.0-Setup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
WizardStyle=modern
ArchitecturesInstallIn64BitMode=x64compatible
UninstallDisplayName=Dragon Tiger Game Link

[Files]
Source: "dist\DragonTigerGameLink\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autodesktop}\Dragon Tiger Game Link"; Filename: "{app}\DragonTigerGameLink.exe"; WorkingDir: "{app}"
Name: "{autoprograms}\Dragon Tiger Game Link"; Filename: "{app}\DragonTigerGameLink.exe"; WorkingDir: "{app}"

[Run]
Filename: "{app}\DragonTigerGameLink.exe"; Description: "Launch Dragon Tiger Game Link"; Flags: nowait postinstall skipifsilent
