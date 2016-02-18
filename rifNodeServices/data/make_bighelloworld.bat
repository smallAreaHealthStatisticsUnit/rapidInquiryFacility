REM Generate 2G bighelloworld.js
cp -f helloworld.js bighelloworld.js
for /L %%i in (1,1,25) do type bighelloworld.js >> bighelloworld.js
ls -lh bighelloworld.js