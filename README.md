
# RusCGStore-backend

## Service-settings

```
[Unit]
Description=CGStore Ktor Backend

[Service]
User=root
Environment=ip=$database_ip
Environment=password=$database_password
Environment=database=$database_name
Environment=user=$database_user
Environment=secret=$jwt_secret
WorkingDirectory=/cgstore
ExecStart=/usr/bin/java -jar ruscgstore.jar

[Install]
WantedBy=multi-user.target
```


