# Tomcat Dual Deployment - Due Applicazioni Web

Questo progetto contiene due semplici applicazioni web Java che possono essere deployate su un unico server Apache Tomcat. Ogni applicazione logga i messaggi ricevuti su un endpoint HTTP.

## Struttura del Progetto

```
.
├── app1/                           # Prima applicazione web
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/app1/
│   │       │       └── LogServlet.java
│   │       └── webapp/
│   │           └── WEB-INF/
│   │               └── web.xml
│   └── pom.xml
│
├── app2/                           # Seconda applicazione web
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/app2/
│   │       │       └── LogServlet.java
│   │       └── webapp/
│   │           └── WEB-INF/
│   │               └── web.xml
│   └── pom.xml
│
├── pom.xml                         # Parent POM
└── README.md
```

## Prerequisiti

- Sistema operativo: Ubuntu (18.04 o superiore)
- Java Development Kit (JDK) 8 o superiore
- Apache Maven
- Apache Tomcat 9 o superiore

## Installazione su Ubuntu

### 1. Aggiornare il Sistema

```bash
sudo apt update
sudo apt upgrade -y
```

### 2. Installare Java (OpenJDK 11)

```bash
sudo apt install openjdk-11-jdk -y
```

Verificare l'installazione:

```bash
java -version
```

Output atteso:
```
openjdk version "11.0.x" ...
```

### 3. Installare Maven

```bash
sudo apt install maven -y
```

Verificare l'installazione:

```bash
mvn -version
```

### 4. Installare Apache Tomcat 9

#### Opzione A: Installazione tramite APT (Più semplice)

```bash
sudo apt install tomcat9 tomcat9-admin -y
```

Tomcat verrà installato in `/var/lib/tomcat9/` e si avvierà automaticamente.

#### Opzione B: Installazione Manuale (Più controllo)

```bash
# Creare un utente per Tomcat
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat

# Scaricare Tomcat 9 (versione 9.0.x)
cd /tmp
wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.82/bin/apache-tomcat-9.0.82.tar.gz

# Estrarre l'archivio
sudo tar xzvf apache-tomcat-9.0.82.tar.gz -C /opt/tomcat --strip-components=1

# Impostare i permessi
sudo chown -R tomcat:tomcat /opt/tomcat/
sudo chmod -R u+x /opt/tomcat/bin
```

Creare un file di servizio systemd:

```bash
sudo nano /etc/systemd/system/tomcat.service
```

Inserire il seguente contenuto:

```ini
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
Environment="CATALINA_PID=/opt/tomcat/temp/tomcat.pid"
Environment="CATALINA_HOME=/opt/tomcat"
Environment="CATALINA_BASE=/opt/tomcat"
Environment="CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC"
Environment="JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
```

Salvare e abilitare il servizio:

```bash
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl start tomcat
```

### 5. Verificare che Tomcat sia in Esecuzione

```bash
sudo systemctl status tomcat
```

Oppure visitare nel browser:
```
http://localhost:8080
```

Dovresti vedere la pagina di benvenuto di Tomcat.

### 6. Configurare Tomcat Manager (Opzionale ma consigliato)

Per l'installazione APT:
```bash
sudo nano /etc/tomcat9/tomcat-users.xml
```

Per l'installazione manuale:
```bash
sudo nano /opt/tomcat/conf/tomcat-users.xml
```

Aggiungere prima del tag `</tomcat-users>`:

```xml
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<user username="admin" password="admin123" roles="manager-gui,manager-script"/>
```

Riavviare Tomcat:

```bash
# Per installazione APT:
sudo systemctl restart tomcat9

# Per installazione manuale:
sudo systemctl restart tomcat
```

## Build delle Applicazioni

### 1. Clonare/Scaricare il Progetto

```bash
cd /home/user
git clone <repository-url>
cd multiservice-process-test
```

### 2. Compilare le Applicazioni

```bash
mvn clean package
```

Questo comando:
- Compila entrambe le applicazioni
- Crea i file WAR in:
  - `app1/target/app1.war`
  - `app2/target/app2.war`

### 3. Verificare i File WAR

```bash
ls -lh app1/target/app1.war
ls -lh app2/target/app2.war
```

## Deployment su Tomcat

### Metodo 1: Deploy Manuale (Copia Diretta)

Per installazione APT:
```bash
sudo cp app1/target/app1.war /var/lib/tomcat9/webapps/
sudo cp app2/target/app2.war /var/lib/tomcat9/webapps/
```

Per installazione manuale:
```bash
sudo cp app1/target/app1.war /opt/tomcat/webapps/
sudo cp app2/target/app2.war /opt/tomcat/webapps/
```

Tomcat deploierà automaticamente le applicazioni in pochi secondi.

### Metodo 2: Deploy tramite Tomcat Manager

1. Accedere a: `http://localhost:8080/manager/html`
2. Inserire le credenziali configurate in precedenza
3. Nella sezione "WAR file to deploy":
   - Selezionare `app1.war`
   - Cliccare "Deploy"
   - Ripetere per `app2.war`

### Metodo 3: Deploy con Maven (richiede configurazione)

Configurare `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>TomcatServer</id>
      <username>admin</username>
      <password>admin123</password>
    </server>
  </servers>
</settings>
```

Deployare:

```bash
# Per app1
cd app1
mvn tomcat7:deploy -Dmaven.tomcat.url=http://localhost:8080/manager/text

# Per app2
cd ../app2
mvn tomcat7:deploy -Dmaven.tomcat.url=http://localhost:8080/manager/text
```

## Verificare il Deployment

### 1. Controllare le Applicazioni Deployate

Accedere al Tomcat Manager:
```
http://localhost:8080/manager/html
```

Dovresti vedere `app1` e `app2` nella lista delle applicazioni in esecuzione.

### 2. Testare le Applicazioni

#### Test App1:

```bash
# GET request
curl http://localhost:8080/app1/log

# Con parametri
curl "http://localhost:8080/app1/log?test=123"
```

Risposta attesa:
```json
{
  "application": "app1",
  "message": "Request logged successfully",
  "timestamp": "2025-12-15 10:30:45",
  "requestURI": "/app1/log",
  "clientIP": "127.0.0.1"
}
```

#### Test App2:

```bash
# GET request
curl http://localhost:8080/app2/log

# Con parametri
curl "http://localhost:8080/app2/log?test=456"
```

Risposta attesa:
```json
{
  "application": "app2",
  "message": "Request logged successfully",
  "timestamp": "2025-12-15 10:31:20",
  "requestURI": "/app2/log",
  "clientIP": "127.0.0.1"
}
```

### 3. Visualizzare i Log

I log vengono scritti sia nel logger Java che sulla console standard.

#### Per installazione APT:

```bash
# Log di Tomcat
sudo tail -f /var/lib/tomcat9/logs/catalina.out

# Log delle applicazioni
sudo journalctl -u tomcat9 -f
```

#### Per installazione manuale:

```bash
sudo tail -f /opt/tomcat/logs/catalina.out
```

Output atteso nei log:
```
[APP1] [2025-12-15 10:30:45] Request received from 127.0.0.1 - URI: /app1/log?test=123
[APP2] [2025-12-15 10:31:20] Request received from 127.0.0.1 - URI: /app2/log?test=456
```

## Gestione di Tomcat

### Avviare Tomcat

```bash
# Per installazione APT:
sudo systemctl start tomcat9

# Per installazione manuale:
sudo systemctl start tomcat
```

### Fermare Tomcat

```bash
# Per installazione APT:
sudo systemctl stop tomcat9

# Per installazione manuale:
sudo systemctl stop tomcat
```

### Riavviare Tomcat

```bash
# Per installazione APT:
sudo systemctl restart tomcat9

# Per installazione manuale:
sudo systemctl restart tomcat
```

### Controllare lo Stato

```bash
# Per installazione APT:
sudo systemctl status tomcat9

# Per installazione manuale:
sudo systemctl status tomcat
```

## Undeploy delle Applicazioni

### Metodo 1: Manuale

```bash
# Per installazione APT:
sudo rm /var/lib/tomcat9/webapps/app1.war
sudo rm -rf /var/lib/tomcat9/webapps/app1/
sudo rm /var/lib/tomcat9/webapps/app2.war
sudo rm -rf /var/lib/tomcat9/webapps/app2/

# Per installazione manuale:
sudo rm /opt/tomcat/webapps/app1.war
sudo rm -rf /opt/tomcat/webapps/app1/
sudo rm /opt/tomcat/webapps/app2.war
sudo rm -rf /opt/tomcat/webapps/app2/
```

### Metodo 2: Tramite Manager

1. Accedere a: `http://localhost:8080/manager/html`
2. Trovare l'applicazione nella lista
3. Cliccare sul pulsante "Undeploy"

## Risoluzione Problemi

### Tomcat non si avvia

```bash
# Verificare i log
sudo journalctl -u tomcat9 -n 50

# Verificare la porta 8080
sudo netstat -tlnp | grep 8080
```

### Applicazione non si deploya

```bash
# Verificare i permessi
ls -la /var/lib/tomcat9/webapps/

# Verificare i log di deployment
sudo tail -f /var/lib/tomcat9/logs/catalina.out
```

### Errore 404 quando accedo all'applicazione

```bash
# Verificare che le applicazioni siano deployate
ls -la /var/lib/tomcat9/webapps/

# Verificare che le directory siano state espanse
ls -la /var/lib/tomcat9/webapps/app1/
ls -la /var/lib/tomcat9/webapps/app2/
```

### Controllare errori Java

```bash
# Verificare la versione di Java
java -version

# Verificare JAVA_HOME
echo $JAVA_HOME
```

## Configurazione Firewall (Se necessario)

```bash
# Aprire la porta 8080
sudo ufw allow 8080/tcp

# Verificare lo stato
sudo ufw status
```

## Note Aggiuntive

- Le applicazioni rispondono su tutti i path incluso il root (`/`) oltre al path specifico `/log`
- I log includono timestamp, IP del client, URI richiesto e parametri query
- Le applicazioni restituiscono JSON come formato di risposta
- Entrambe le applicazioni supportano sia richieste GET che POST

## Autore

Questo progetto è stato creato come esempio di deployment multiplo su Tomcat.

## Licenza

Vedere il file LICENSE per i dettagli.
