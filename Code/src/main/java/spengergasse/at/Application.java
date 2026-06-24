package spengergasse.at;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import spengergasse.at.service.CsvService;
import spengergasse.at.service.ScriptureManager;

@SpringBootApplication
@EnableVaadin("spengergasse.at.views")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ScriptureManager scriptureManager() {
        ScriptureManager manager = new ScriptureManager();
        CsvService csv = new CsvService();
        try {
            int loaded = csv.loadAll(manager);
            System.out.println("Scripture Space gestartet – " + loaded + " Einträge geladen.");
        } catch (Exception e) {
            System.out.println("Keine gespeicherten Daten gefunden, starte neu.");
        }
        return manager;
    }

    @Bean
    public CsvService csvService() {
        return new CsvService();
    }
}
