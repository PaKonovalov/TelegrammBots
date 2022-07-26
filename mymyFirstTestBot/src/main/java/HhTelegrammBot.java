import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import entry.Hh;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HhTelegrammBot {

    TelegramBot bot;

    // Create your bot passing the token received from @BotFather
    HhTelegrammBot() {

        this.bot = new TelegramBot("5400043733:AAHPaxpsbvT2oAxOXIczHhaSaLWUjzncXJE");
    }

    public void listen() {
        // Register for updates
        this.bot.setUpdatesListener(element ->

        {
            // ... process
            // return id of last processed update or confirm them all
            System.out.println(element);
            String searchURI = "https://api.hh.ru/vacancies?text=";
            String myArea = "&area=2";
            element.forEach(it -> {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(searchURI + it.message().text().replace(" ", "%20") + myArea))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String body = response.body();
                    System.out.println(body);
                    Hh hh = mapper.readValue(body, Hh.class);
                    hh.getItems().forEach(Job -> { //.subList(0, 10) для ограничения поиска до 10 вариантов
                        bot.execute(new SendMessage(it.message().chat().id(), "Вакансия: " + Job.getName() + "\nСсылка http://hh.ru/vacancy/" + Job.getId()));
                        System.out.println(Job.getId() + " " + Job.getName());
                    });

                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                }

            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}

