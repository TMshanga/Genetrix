for /f "eol=: delims=" %%F in ('dir /b /od Genetrix*.jar') do @set "newest=%%F"
start javaw -jar "%newest%"
