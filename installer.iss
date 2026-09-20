[Setup]
AppName=Dragon Tiger Analyzer v2.0
AppVersion=2.0.0
DefaultDirName={autopf}\Dragon Tiger Analyzer v2.0
DefaultGroupName=Dragon Tiger Analyzer v2.0
OutputDir=installer
OutputBaseFilename=DragonTigerAnalyzerPC-v2.0-FINAL-Setup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
WizardStyle=modern
ArchitecturesInstallIn64BitMode=x64compatible
UninstallDisplayName=Dragon Tiger Analyzer v2.0

[Files]
Source: "dist\DragonTigerAnalyzerPC\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autodesktop}\Dragon Tiger Analyzer v2.0 FINAL"; Filename: "{app}\DragonTigerAnalyzerPC.exe"; WorkingDir: "{app}"
Name: "{autoprograms}\Dragon Tiger Analyzer v2.0 FINAL"; Filename: "{app}\DragonTigerAnalyzerPC.exe"; WorkingDir: "{app}"

[Run]
Filename: "{app}\DragonTigerAnalyzerPC.exe"; Description: "Launch Dragon Tiger Analyzer"; Flags: nowait postinstall skipifsilent
