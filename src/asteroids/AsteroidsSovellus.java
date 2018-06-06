package asteroids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AsteroidsSovellus extends Application {

    public static int LEVEYS = 300;
    public static int KORKEUS = 200;

    @Override
    public void start(Stage ikkuna) {

        Pane ruutu = new Pane();
        Image kuva = new Image("file:avaruus.jpg");
        ImageView avaruus = new ImageView(kuva);
        AudioClip shotgun = new AudioClip("file:shotgun.wav");
        AudioClip musat = new AudioClip("file:scifi.wav");
        Text text = new Text(10, 20, "Points: 0");
        text.setFill(Color.WHITE);
        AtomicInteger pisteet = new AtomicInteger();
        ruutu.getChildren().add(avaruus);
        ruutu.getChildren().add(text);
        ruutu.setPrefSize(LEVEYS, KORKEUS);
        Alus alus = new Alus(LEVEYS / 2, KORKEUS / 2);
        List<Ammus> ammukset = new ArrayList<>();
        List<Asteroidi> asteroidit = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Random rnd = new Random();
            Asteroidi asteroidi = new Asteroidi(rnd.nextInt(LEVEYS / 3), rnd.nextInt(KORKEUS));
            asteroidit.add(asteroidi);
        }
        asteroidit.forEach(asteroidi -> ruutu.getChildren().add(asteroidi.getHahmo()));
        asteroidit.forEach(asteroidi -> asteroidi.kaannaOikealle());
        asteroidit.forEach(asteroidi -> asteroidi.kiihdyta());
       
        ruutu.getChildren().add(alus.getHahmo());
        Scene nakyma = new Scene(ruutu);

        Map<KeyCode, Boolean> nappiPohjassa = new HashMap<>();

        nakyma.setOnKeyPressed(value -> {
            nappiPohjassa.put(value.getCode(), Boolean.TRUE);
        });

        nakyma.setOnKeyReleased(value -> {
            nappiPohjassa.put(value.getCode(), Boolean.FALSE);
        });

        new AnimationTimer() {
            @Override
            public void handle(long nykyhetki) {
                musat.play();
                //      text.setText("Pisteet: " + pisteet.incrementAndGet());
                if (nappiPohjassa.getOrDefault(KeyCode.LEFT, false)) {
                    alus.kaannaVasemmalle();
                }
                if (nappiPohjassa.getOrDefault(KeyCode.RIGHT, true)) {
                    alus.kaannaOikealle();
                }
                if (nappiPohjassa.getOrDefault(KeyCode.UP, false)) {
                    alus.kiihdyta();
                }
                if (nappiPohjassa.getOrDefault(KeyCode.SPACE, false) && ammukset.size() < 3) {
                    // ammutaan
                    shotgun.play();
                    Ammus ammus = new Ammus((int) alus.getHahmo().getTranslateX(), (int) alus.getHahmo().getTranslateY());
                    ammus.getHahmo().setRotate(alus.getHahmo().getRotate());
                    ammukset.add(ammus);

                    ammus.kiihdyta();
                    ammus.setLiike(ammus.getLiike().normalize().multiply(3));

                    ruutu.getChildren().add(ammus.getHahmo());
                }
                ammukset.forEach(ammus -> {
                    List<Asteroidi> tormatyt = asteroidit.stream()
                            .filter(asteroidi -> asteroidi.tormaa(ammus))
                            .collect(Collectors.toList());

                    tormatyt.stream().forEach(tormatty -> {
                        asteroidit.remove(tormatty);
                        ruutu.getChildren().remove(tormatty.getHahmo());
                    });
                });

                alus.liiku();
                ammukset.forEach(ammus -> ammus.liiku());

                asteroidit.forEach(asteroidi -> asteroidi.liiku());
                //asteroidi.liiku();
                asteroidit.forEach(asteroidi -> {
                    if (alus.tormaa(asteroidi)) {
                        stop();
                    }
                });
                ammukset.forEach(ammus -> {
                    asteroidit.forEach(asteroidi -> {
                        if (ammus.tormaa(asteroidi)) {
                            ammus.setElossa(false);
                            asteroidi.setElossa(false);
                        }
                    });
                    if (!ammus.isElossa()) {
                        text.setText("Points: " + pisteet.addAndGet(1000));
                    }
                });

                ammukset.stream()
                        .filter(ammus -> !ammus.isElossa())
                        .forEach(ammus -> ruutu.getChildren().remove(ammus.getHahmo()));
                ammukset.removeAll(ammukset.stream()
                        .filter(ammus -> !ammus.isElossa())
                        .collect(Collectors.toList()));

                asteroidit.stream()
                        .filter(asteroidi -> !asteroidi.isElossa())
                        .forEach(asteroidi -> ruutu.getChildren().remove(asteroidi.getHahmo()));
                asteroidit.removeAll(asteroidit.stream()
                        .filter(asteroidi -> !asteroidi.isElossa())
                        .collect(Collectors.toList()));
                
                if (Math.random() < 0.005) {
                    Asteroidi asteroidi = new Asteroidi(LEVEYS, KORKEUS);
                    if (!asteroidi.tormaa(alus)) {
                        asteroidit.add(asteroidi);
                        ruutu.getChildren().add(asteroidi.getHahmo());
                    }
                }
            }

        }.start();

        ikkuna.setTitle("Asteroidi");
        ikkuna.setScene(nakyma);
        ikkuna.show();
    }

    public static void main(String[] args) {
        launch(AsteroidsSovellus.class);
    }

    public static int osiaToteutettu() {
        return 4;
    }

}
