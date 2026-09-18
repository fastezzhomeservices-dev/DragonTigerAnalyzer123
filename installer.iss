; Inno Setup installer for Dragon Tiger Analyzer
[Setup]
AppName=Dragon Tiger Analyzer
AppVersion=1.0.0
DefaultDirName={autopf}\Dragon Tiger Analyzer
DefaultGroupName=Dragon Tiger Analyzer
OutputDir=installer
OutputBaseFilename=DragonTigerAnalyzerPC-Setup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
WizardStyle=modern

[Files]
Source: "dist\DragonTigerAnalyzerPC.exe"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{autodesktop}\Dragon Tiger Analyzer"; Filename: "{app}\DragonTigerAnalyzerPC.exe"; WorkingDir: "{app}"
Name: "{autoprograms}\Dragon Tiger Analyzer"; Filename: "{app}\DragonTigerAnalyzerPC.exe"; WorkingDir: "{app}"

[Run]
Filename: "{app}\DragonTigerAnalyzerPC.exe"; Description: "Launch Dragon Tiger Analyzer"; Flags: nowait postinstall skipifsilent
