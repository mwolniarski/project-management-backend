package pl.wolniarskim.project_management.services;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Getter
@Service
public class EmailTemplateService {

    public String confirmationEmailTemplate;

    public EmailTemplateService() {
        loadConfirmationEmailTemplate();
    }

    private void loadConfirmationEmailTemplate(){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/emails/templates/confirmation_email_template.html"));
            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        confirmationEmailTemplate = stringBuilder.toString();
    }
}
