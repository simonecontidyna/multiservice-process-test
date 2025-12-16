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

**NOTA:** Se il pacchetto `tomcat9` non è disponibile nei repository APT della tua versione di Ubuntu, usa l'installazione manuale (consigliata).

```bash
# Creare un utente dedicato per Tomcat
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat

# Scaricare Tomcat 9 (ultima versione stabile - 9.0.113)
cd /tmp
wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.113/bin/apache-tomcat-9.0.113.tar.gz

# Se il download fallisce, prova con un mirror alternativo:
# wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.113/bin/apache-tomcat-9.0.113.tar.gz

# Creare la directory di installazione
sudo mkdir -p /opt/tomcat

# Estrarre l'archivio
sudo tar xzvf apache-tomcat-9.0.113.tar.gz -C /opt/tomcat --strip-components=1

# Impostare i permessi corretti
sudo chown -R tomcat:tomcat /opt/tomcat/
sudo chmod -R u+x /opt/tomcat/bin
```

#### Configurare JAVA_HOME

Prima di creare il servizio, verifica il path di Java:

```bash
# Trova il path di Java
sudo update-alternatives --config java
```

Oppure:

```bash
# Mostra tutti i JDK installati
ls -la /usr/lib/jvm/
```

**IMPORTANTE:** Il path di JAVA_HOME dipende dall'architettura del sistema:
- **x86_64/AMD64**: `/usr/lib/jvm/java-11-openjdk-amd64`
- **ARM64/aarch64**: `/usr/lib/jvm/java-11-openjdk-arm64`

Per identificare l'architettura del tuo sistema:
```bash
uname -m
# Output: x86_64 (AMD64) oppure aarch64 (ARM64)
```

#### Creare il Servizio Systemd

```bash
sudo nano /etc/systemd/system/tomcat.service
```

Inserire il seguente contenuto:

**IMPORTANTE:** Usa il path corretto per JAVA_HOME in base alla tua architettura:
- Per AMD64: `JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64`
- Per ARM64: `JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64`

```ini
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

# Per AMD64 usa: Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
# Per ARM64 usa: Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64"
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

Salvare il file (CTRL+O, ENTER, CTRL+X in nano) e abilitare il servizio:

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

Modificare il file di configurazione degli utenti:

```bash
sudo nano /opt/tomcat/conf/tomcat-users.xml
```

Aggiungere le seguenti righe prima del tag di chiusura `</tomcat-users>`:

```xml
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<user username="admin" password="admin123" roles="manager-gui,manager-script"/>
```

**IMPORTANTE:** Cambia la password `admin123` con una password sicura in ambienti di produzione.

Per permettere l'accesso al Manager anche da host remoti (opzionale, solo per sviluppo):

```bash
# Commentare le restrizioni di accesso
sudo nano /opt/tomcat/webapps/manager/META-INF/context.xml
```

Commentare o rimuovere la sezione `<Valve>`:

```xml
<!--
<Valve className="org.apache.catalina.valves.RemoteAddrValve"
       allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1" />
-->
```

Riavviare Tomcat:

```bash
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

### Metodo 1: Deploy Manuale (Copia Diretta) - CONSIGLIATO

```bash
sudo cp app1/target/app1.war /opt/tomcat/webapps/
sudo cp app2/target/app2.war /opt/tomcat/webapps/
```

Tomcat deploierà automaticamente le applicazioni in pochi secondi. Puoi monitorare il processo con:

```bash
sudo tail -f /opt/tomcat/logs/catalina.out
```

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
  "service": "tomcat-app1",
  "dt.entity.service": "SERVICE-APP1",
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
  "service": "tomcat-app2",
  "dt.entity.service": "SERVICE-APP2",
  "message": "Request logged successfully",
  "timestamp": "2025-12-15 10:31:20",
  "requestURI": "/app2/log",
  "clientIP": "127.0.0.1"
}
```

### 3. Visualizzare i Log

I log vengono scritti sia nel logger Java che sulla console standard.

```bash
# Log di Tomcat (output principale)
sudo tail -f /opt/tomcat/logs/catalina.out

# Log di sistema tramite journalctl
sudo journalctl -u tomcat -f

# Altri log di Tomcat
ls -lh /opt/tomcat/logs/
```

Output atteso nei log (con Dynatrace enrichment):
```
[!dt dt.entity.service=SERVICE-APP1] 2025-12-15 21:45:12.345 [INFO] com.example.app1.LogServlet - [APP1] Request received from 127.0.0.1 - URI: /app1/log?test=123
[!dt dt.entity.service=SERVICE-APP2] 2025-12-15 21:45:20.678 [INFO] com.example.app2.LogServlet - [APP2] Request received from 127.0.0.1 - URI: /app2/log?test=456
```

**Nota Dynatrace:** I log includono metadati di enrichment tramite un **custom PatternFormatter** nel formato `[!dt dt.entity.service=SERVICE-ID]` per l'integrazione con Dynatrace log monitoring. Ogni applicazione ha:
- `app1`: Service ID `SERVICE-APP1` (service name: `tomcat-app1`)
- `app2`: Service ID `SERVICE-APP2` (service name: `tomcat-app2`)

Il formatter viene configurato automaticamente all'avvio del servlet tramite `java.util.logging.Formatter`.

## Gestione di Tomcat

### Avviare Tomcat

```bash
sudo systemctl start tomcat
```

### Fermare Tomcat

```bash
sudo systemctl stop tomcat
```

### Riavviare Tomcat

```bash
sudo systemctl restart tomcat
```

### Controllare lo Stato

```bash
sudo systemctl status tomcat
```

### Verificare che Tomcat sia in ascolto

```bash
# Verificare che la porta 8080 sia in ascolto
sudo netstat -tlnp | grep 8080

# Oppure con ss
sudo ss -tlnp | grep 8080
```

## Undeploy delle Applicazioni

### Metodo 1: Manuale

```bash
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
# Verificare i log di sistema
sudo journalctl -u tomcat -n 50 --no-pager

# Verificare i log di Tomcat
sudo cat /opt/tomcat/logs/catalina.out

# Verificare la porta 8080
sudo netstat -tlnp | grep 8080

# Verificare che Java sia installato
java -version

# Verificare il path di JAVA_HOME nel servizio
grep JAVA_HOME /etc/systemd/system/tomcat.service

# Testare l'avvio manuale
sudo su - tomcat -s /bin/bash -c '/opt/tomcat/bin/catalina.sh run'
```

**Errore: "JAVA_HOME is not defined correctly"**

Questo errore indica che il path di JAVA_HOME non è corretto per la tua architettura:

```bash
# Verificare l'architettura del sistema
uname -m

# Verificare quale JDK è installato
ls -la /usr/lib/jvm/

# Correggere il path in base all'architettura
# Per ARM64:
sudo sed -i 's|JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64|JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64|g' /etc/systemd/system/tomcat.service

# Per AMD64 (se necessario):
sudo sed -i 's|JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64|JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64|g' /etc/systemd/system/tomcat.service

# Ricaricare e riavviare
sudo systemctl daemon-reload
sudo systemctl restart tomcat
```

### Applicazione non si deploya

```bash
# Verificare i permessi
ls -la /opt/tomcat/webapps/

# Verificare che i file WAR siano presenti
ls -lh /opt/tomcat/webapps/*.war

# Verificare i log di deployment
sudo tail -f /opt/tomcat/logs/catalina.out

# Verificare errori specifici delle applicazioni
sudo grep -i "error\|exception" /opt/tomcat/logs/catalina.out
```

### Errore 404 quando accedo all'applicazione

```bash
# Verificare che le applicazioni siano deployate
ls -la /opt/tomcat/webapps/

# Verificare che le directory siano state espanse
ls -la /opt/tomcat/webapps/app1/
ls -la /opt/tomcat/webapps/app2/

# Verificare i descrittori web.xml
cat /opt/tomcat/webapps/app1/WEB-INF/web.xml
cat /opt/tomcat/webapps/app2/WEB-INF/web.xml
```

### Errori di permessi

```bash
# Correggere i permessi
sudo chown -R tomcat:tomcat /opt/tomcat/
sudo chmod -R u+x /opt/tomcat/bin
sudo chmod -R u+r /opt/tomcat/
```

### Controllare errori Java

```bash
# Verificare la versione di Java
java -version

# Verificare tutti i JDK installati
ls -la /usr/lib/jvm/

# Verificare JAVA_HOME nel contesto del servizio
sudo systemctl show tomcat | grep JAVA_HOME
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

### Integrazione Dynatrace

Le applicazioni includono log enrichment per Dynatrace tramite **Log4j2 con MDC (Mapped Diagnostic Context)**:

#### Implementazione

- **Framework**: Apache Log4j2 2.23.1
- **Configurazione**: File `log4j2.xml` in `src/main/resources/` per entrambe le applicazioni
- **MDC (ThreadContext)**: `dt.entity.service` viene impostato dinamicamente per ogni richiesta
- **Pattern Log4j2**: `[!dt dt.entity.service=%X{dt.entity.service}] %d{yyyy-MM-dd HH:mm:ss.SSS} [%level] %logger{36} - %msg%n`

#### Configurazione log4j2.xml

```xml
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <!-- Dynatrace log enrichment pattern with MDC -->
            <PatternLayout pattern="[!dt dt.entity.service=%X{dt.entity.service}] %d{yyyy-MM-dd HH:mm:ss.SSS} [%level] %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

#### Codice Servlet (esempio)

```java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

// Set Dynatrace entity service in MDC
ThreadContext.put("dt.entity.service", "SERVICE-APP1");
logger.info("Request received");
ThreadContext.clearAll(); // Clear after use
```

#### Service IDs

- **app1**: Service ID `SERVICE-APP1` (service name: `tomcat-app1`)
- **app2**: Service ID `SERVICE-APP2` (service name: `tomcat-app2`)

#### Campi JSON

Le risposte HTTP JSON includono:
- `service`: Nome del servizio (es. `tomcat-app1`)
- `dt.entity.service`: ID Dynatrace per la correlazione (es. `SERVICE-APP1`)

#### Vantaggi di Log4j2 con MDC

- ✅ Pattern standard raccomandato da Dynatrace
- ✅ Supporto nativo per MDC/ThreadContext
- ✅ Configurazione dichiarativa tramite XML
- ✅ Alte performance e gestione asincrona dei log
- ✅ Estensibile per aggiungere altri campi Dynatrace (`dt.trace_id`, `dt.span_id`, ecc.)

#### Riferimenti

- [Dynatrace Log Enrichment Documentation](https://docs.dynatrace.com/docs/analyze-explore-automate/logs/lma-log-enrichment)
- [Apache Log4j2 Pattern Layout](https://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout)
- [GitHub - dynatrace-oss/log4j-metadata-provider](https://github.com/dynatrace-oss/log4j-metadata-provider)

## Autore

Questo progetto è stato creato come esempio di deployment multiplo su Tomcat.

## Licenza

Vedere il file LICENSE per i dettagli.
