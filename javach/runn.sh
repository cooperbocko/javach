echo "running nameserver $1"
java -cp target/javach-1.0-SNAPSHOT.jar com.mycompany.nameserver.Nameserver src/main/java/com/mycompany/nameserver/nsconfig$1.txt
