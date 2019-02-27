# -*- mode: python -*-

block_cipher = None

added_files = [ ("install.ini", "."),
                ("../rifDatabase/SQLserver", "SQLserver"),
                ("../rifDatabase/Postgres", "Postgres"),
                ("../rifDatabase/GeospatialData", "GeospatialData"),
                ("../rifDatabase/DataLoaderData", "DataLoaderData"),
                ("../rifServices/target/rifServices.war", "warfiles"),
                ("../taxonomyServices/target/taxonomies.war", "warfiles"),
                ("../statsService/target/statistics.war", "warfiles"),
                ("../rifWebApplication/target/RIF40.war", "warfiles")
            ]
# Separate installer names for the different platforms
import platform
installer_names = {"Linux": "rifInstaller_linux",
                   "Darwin": "rifInstaller_mac",
                   "Windows": "rifInstaller"}
installer_name = installer_names[platform.system()]

a = Analysis(['install.py'],
            pathex=['.'],
            binaries=[],
            datas=added_files,
            hiddenimports=[],
            hookspath=[],
            runtime_hooks=[],
            excludes=[],
            win_no_prefer_redirects=False,
            win_private_assemblies=False,
            cipher=block_cipher,
            noarchive=False)
pyz = PYZ(a.pure, a.zipped_data,
             cipher=block_cipher)
exe = EXE(pyz,
          a.scripts,
          a.binaries,
          a.zipfiles,
          a.datas,
          [],
          name=installer_name,
          debug=False,
          bootloader_ignore_signals=False,
          strip=False,
          upx=True,
          runtime_tmpdir=None,
          console=True )

#app = BUNDLE(exe,
#             name='rifInstaller.app',
#             icon=None,
#             bundle_identifier=None)
