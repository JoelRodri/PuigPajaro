package com.mygdx.puig_pajaro;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final PuigPajaro game;
    Texture backgroundImage;
    Texture pipeUpImage;
    Texture pipeDownImage;
    Texture malo;
    OrthographicCamera camera;
    Texture birdImage;
    Rectangle player;
    boolean dead;
    boolean pausar;
    boolean respawn;
    boolean direccion;
    float speedy;
    float gravity;
    float score;
    Array<Rectangle> obstacles;
    Array<enemigo> malos;
    int lastObstacleTime;
    int lastBoss;
    Sound flapSound;
    Sound failSound;
    Texture pausa2;
    Rectangle pausa;
    int spawn;

    public GameScreen(final PuigPajaro game) {
        this.game = game;
        score = 0;
        dead = false;
        respawn = false;
        pausar = false;
        direccion = false;
        pausa2 = new Texture(Gdx.files.internal("pausa.png"));
        backgroundImage = new Texture(Gdx.files.internal("background.png"));
        flapSound = Gdx.audio.newSound(Gdx.files.internal("flap.wav"));
        failSound = Gdx.audio.newSound(Gdx.files.internal("fail.wav"));
        pipeUpImage = new Texture(Gdx.files.internal("pipe_up.png"));
        pipeDownImage = new Texture(Gdx.files.internal("pipe_down.png"));
        malo = new Texture(Gdx.files.internal("birdmalo.png"));
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        birdImage = new Texture(Gdx.files.internal("bird.png"));
        obstacles = new Array<Rectangle>();
        malos = new Array<enemigo>();
        spawnObstacle();

        pausa = new Rectangle();
        pausa.x = 725;
        pausa.y = 430;
        pausa.width = 35;
        pausa.height = 35;

        player = new Rectangle();
        player.x = 200;
        player.y = 480 / 2 - 64 / 2;
        player.width = 64;
        player.height = 45;
        score = 0;
        speedy = 0;
        gravity = 850f;

        spawn = 0;
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.3f, 0.8f, 0.8f, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0);
        game.batch.draw(birdImage, player.x, player.y);

        for (int i = 0; i < obstacles.size; i++) {
            game.batch.draw(
                    i % 2 == 0 ? pipeUpImage : pipeDownImage,
                    obstacles.get(i).x, obstacles.get(i).y);
        }

        for (int i = 0; i < malos.size; i++) {
            game.batch.draw(malo, malos.get(i).cuerpo.x, malos.get(i).cuerpo.y);
        }

        game.batch.draw(pausa2, 725, 430);
        game.font.draw(game.batch, "Score: " + (int) score, 10, 470);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            speedy = 400f;
            flapSound.play();

            if (pausa.contains(touchPos.x, touchPos.y)) {
                if (pausar) {
                    pausar = false;
                } else {
                    pausar = true;
                }
            }
        }


        if (!pausar) {
            spawn++;
            player.y += speedy * Gdx.graphics.getDeltaTime();
            speedy -= gravity * Gdx.graphics.getDeltaTime();
            score += Gdx.graphics.getDeltaTime();


            if (player.y > 480 - 45) {
                player.y = 480 - 45;
            }
            if (player.y < 0 - 45) {
                pausar = true;
                birdImage = new Texture(Gdx.files.internal("birdmuerto.png"));
                dead = true;
                failSound.play();
            }


            if (spawn - lastObstacleTime > 100) spawnObstacle();


            if(!respawn){
                if ((int) score % 10 == 0 && score > 1) {
                        //if (spawn - lastBoss > 30)
                    for (int i = 0; i < 1; i++) {
                        spawnBoss(i);
                    }
                    respawn = true;
                }
            }



            Iterator<Rectangle> iter = obstacles.iterator();
            while (iter.hasNext()) {
                Rectangle tuberia = iter.next();
                tuberia.x -= 200 * Gdx.graphics.getDeltaTime();
                if (tuberia.x < -64) {
                    iter.remove();
                }
                if (tuberia.overlaps(player)) {
                    pausar = true;
                    birdImage = new Texture(Gdx.files.internal("birdmuerto.png"));
                    dead = true;
                    failSound.play();
                }
            }

            Iterator<enemigo> iter2 = malos.iterator();
            while (iter2.hasNext()) {
                enemigo bonus = iter2.next();
                bonus.cuerpo.x -= 200 * Gdx.graphics.getDeltaTime();
                if (bonus.cuerpo.x < -64) {
                    iter2.remove();
                    if (!iter2.hasNext()) {
                        respawn = false;
                    }
                }

                if (bonus.tiempoCambio > 0) {
                    bonus.tiempoCambio-=Gdx.graphics.getDeltaTime();
                    if(bonus.direccion)
                        bonus.cuerpo.y -= 50 * Gdx.graphics.getDeltaTime();
                    else
                        bonus.cuerpo.y += 50 * Gdx.graphics.getDeltaTime();
                } else {
                    bonus.tiempoCambio = (int) (Math.random()*60)+20;
                    bonus.direccion = !bonus.direccion;
                }

                if (bonus.cuerpo.y < 100) {
                    bonus.direccion = !bonus.direccion;
                }
                if (bonus.cuerpo.y > 300) {
                    bonus.direccion = !bonus.direccion;
                }

                if (bonus.cuerpo.overlaps(player)) {
                    score += 10;
                    iter2.remove();
                    respawn = false;

                }
            }
        }

        if (dead) {

            pausar = true;
            player.y += speedy * Gdx.graphics.getDeltaTime();
            speedy -= gravity * Gdx.graphics.getDeltaTime();


            game.lastScore = (int) score;
            if (game.lastScore > game.topScore) {
                game.topScore = game.lastScore;
            }
            if (player.y < 0) {
                game.setScreen(new GameOverScreen(game));
                dispose();
            }
        }

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        pipeUpImage.dispose();
        pipeDownImage.dispose();
        failSound.dispose();
        flapSound.dispose();
    }

    private void spawnObstacle() {
        // Calcula la alçada de l'obstacle aleatòriament
        float holey = MathUtils.random(50, 230);
        // Crea dos obstacles: Una tubería superior i una inferior
        Rectangle pipe1 = new Rectangle();
        pipe1.x = 800;
        pipe1.y = holey - 230;
        pipe1.width = 64;
        pipe1.height = 230;
        obstacles.add(pipe1);
        Rectangle pipe2 = new Rectangle();
        pipe2.x = 800;
        pipe2.y = holey + 200;
        pipe2.width = 64;
        pipe2.height = 230;
        obstacles.add(pipe2);
        lastObstacleTime = spawn;
    }

    private void spawnBoss(int i) {

        enemigo pipe1 = new enemigo();
        pipe1.cuerpo.x = 800 + (150*i);
        pipe1.cuerpo.y = 480 / 2 - 64 / 2;
        pipe1.cuerpo.width = 64;
        pipe1.cuerpo.height = 45;
        malos.add(pipe1);
        lastBoss = spawn;

    }
}