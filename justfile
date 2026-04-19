set shell := ["powershell.exe", "-NoLogo", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command"]

run profile="dev":
    & ".\\tools\\run-app.ps1" "{{profile}}"

stop:
    & ".\\tools\\stop-app.ps1"

status:
    & ".\\tools\\status-app.ps1"

test:
    & ".\\tools\\invoke-mvn.ps1" test

package:
    & ".\\tools\\invoke-mvn.ps1" clean package

db-connect:
    & ".\\tools\\db-connect.ps1"

java-use version:
    & ".\\tools\\java-use.ps1" "{{version}}"
