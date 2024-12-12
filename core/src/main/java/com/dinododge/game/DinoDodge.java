package com.dinododge.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import main.java.com.dinododge.game.Question;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DinoDodge implements ApplicationListener {
    SpriteBatch batch;
    FitViewport viewport;
    Texture backgroundTexture, dinoTexture, meteorTexture, paperTexture;
    Sprite dinoSprite;
    Vector2 touchPos;
    Array<Sprite> meteorSprites, paperSprites;
    float meteorTimer;
    Rectangle dinoRectangle, meteorRectangle, paperRectangle;
    int score, life, maxPapers, currentPapers;
    Array<Question> quizQuestions;
    boolean isQuizActive;
    Question currentQuestion;

    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    BitmapFont bitmap;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        backgroundTexture = new Texture("bg.jpg");
        dinoTexture = new Texture("dino.png");
        meteorTexture = new Texture("meteor.png");
        paperTexture = new Texture("paper.png");
        dinoSprite = new Sprite(dinoTexture);
        dinoSprite.setSize(1, 1);
        touchPos = new Vector2();
        meteorSprites = new Array<>();
        paperSprites = new Array <>();
        dinoRectangle = new Rectangle();
        meteorRectangle = new Rectangle();
        paperRectangle = new Rectangle();
        score = 0;
        life = 3;
        maxPapers = 1;
        currentPapers = 0;

        generator = new FreeTypeFontGenerator(Gdx.files.internal("Roboto-Medium.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 100;
        parameter.borderWidth = 15;
        parameter.borderColor = Color.BLACK;
        parameter.color = Color.WHITE;
        bitmap = generator.generateFont(parameter);

        quizQuestions = new Array<>();
        createQuestions();
        isQuizActive = false;

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {

        if(isQuizActive) {
            drawQuiz();
            handleQuizInput();
        } else {
            input();
            logic();
            draw();
        }


    }

    private void input() {
        
        float speed = .25f;
        float delta = Gdx.graphics.getDeltaTime();

        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            dinoSprite.setCenterX(touchPos.x);
        }
    }

    private void logic() {
        float worldHeight = viewport.getWorldHeight();
        float worldWidth = viewport.getWorldWidth();
        
        float dinoWidth = dinoSprite.getWidth();
        float dinoHeight = dinoSprite.getHeight();

        dinoSprite.setX(MathUtils.clamp(dinoSprite.getX(), 0, worldWidth - dinoWidth));

        float delta = Gdx.graphics.getDeltaTime();

        dinoRectangle.set(dinoSprite.getX(), dinoSprite.getY(), dinoWidth, dinoHeight);

        for (int i = meteorSprites.size - 1; i >= 0; i--) {
            Sprite meteorSprite = meteorSprites.get(i);
            float meteorWidth = meteorSprite.getWidth();
            float meteorHeight = meteorSprite.getHeight();
            
            meteorSprite.translateY(-2f * delta);
            
            meteorRectangle.set(meteorSprite.getX(), meteorSprite.getY(), meteorWidth, meteorHeight);
            
            // perde a gota
            if(meteorSprite.getY() < -meteorHeight) {
                meteorSprites.removeIndex(i);
                // colisao
            } else if (dinoRectangle.overlaps(meteorRectangle)) {
                life--;
                meteorSprites.removeIndex(i);
                increaseDifficulty();
            }
        }

        for (int j = paperSprites.size - 1; j >= 0; j--) {
            Sprite paperSprite = paperSprites.get(j);
            float paperWidth = paperSprite.getWidth();
            float paperHeight = paperSprite.getHeight();
            paperSprite.translateY(-2f * delta);
            paperRectangle.set(paperSprite.getX(), paperSprite.getY(), paperWidth, paperHeight);
    
            // perde o papel
            if(paperSprite.getY() < -paperHeight) {
                paperSprites.removeIndex(j);
                currentPapers--;
                //colisao
            } else if (dinoRectangle.overlaps(paperRectangle)) {
                paperSprites.removeIndex(j);
                currentPapers--;
                showQuiz();
                break;
            }    
        }

        
        meteorTimer += delta;
        if (meteorTimer > 1f) {
            meteorTimer = 0;
            createMeteor();
            createPaper();
        }

        checkGameOver();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        
        batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);

        bitmap.draw(batch, "score: " + score, 20, Gdx.graphics.getHeight() - 20);
        bitmap.draw(batch, "life: " + life, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);

        dinoSprite.draw(batch);
        
        // desenha os meteoros
        for (Sprite meteorSprite : meteorSprites) {
            meteorSprite.draw(batch);
        }

        for (Sprite paperSprite : paperSprites) {
            paperSprite.draw(batch);
        }


        batch.end();
    }


    private void createMeteor() {
        float meteorWidth = 0.7f;
        float meteorHeight = 0.7f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        
        // create the sprite
        Sprite meteorSprite = new Sprite(meteorTexture);
        meteorSprite.setSize(meteorWidth, meteorHeight);
        meteorSprite.setX(MathUtils.random(0f, worldWidth - meteorWidth));
        meteorSprite.setY(worldHeight);
        meteorSprites.add(meteorSprite);

    }

    private void createPaper() {

        if (currentPapers < maxPapers) {
            float paperWidth = 0.7f;
            float paperHeight = 0.7f;
            float worldWidth = viewport.getWorldWidth();
            float worldHeight = viewport.getWorldHeight();
            
            // create the sprite
            Sprite paperSprite = new Sprite(paperTexture);
            paperSprite.setSize(paperWidth, paperHeight);
            paperSprite.setX(MathUtils.random(0f, worldWidth - paperWidth));
            paperSprite.setY(worldHeight);
            paperSprites.add(paperSprite);
            currentPapers++;
        }

    }
    
    private void createQuestions() {
        quizQuestions = new Array<>();
    
        Array<String> options1 = new Array<>();
        options1.add("Meteorito");
        options1.add("Dinossauro");
        options1.add("Vulcão");
        quizQuestions.add(new Question("O que extinguiu os dinossauros?", options1, 0));
    
        Array<String> options2 = new Array<>();
        options2.add("Jurássico");
        options2.add("Cretáceo");
        options2.add("Triássico");
        quizQuestions.add(new Question("Em qual período os dinossauros foram extintos?", options2, 1));
    }

    private Question getRandomQuestion() {
        if (quizQuestions.size == 0) return null; // Sem perguntas disponíveis
        return quizQuestions.random();
    }

    private void increaseDifficulty() {
        // Aumenta a velocidade dos meteoros
        for (Sprite meteorSprite : meteorSprites) {
            meteorSprite.translateY(-0.5f); // Aumenta a velocidade de queda
        }
        
        // Adiciona mais meteoros ao jogo
        for (int i = 0; i < 3; i++) {
            createMeteor();
        }
    }

    private void drawQuiz() {
        ScreenUtils.clear(Color.DARK_GRAY); // Fundo do quiz
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        
        bitmap.setColor(Color.WHITE); // Certifica-se de que o texto será visível
        bitmap.draw(batch, currentQuestion.questionText, 20, worldHeight - 50); // Pergunta
        for (int i = 0; i < currentQuestion.options.size; i++) {
            bitmap.draw(batch, (i + 1) + ": " + currentQuestion.options.get(i), 20, worldHeight - 100 - (i * 30)); // Opções
        }
        
        batch.end();
    }

    private void handleQuizInput() {
        if (!isQuizActive) return;
    
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            processAnswer(0); // Resposta 1
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            processAnswer(1); // Resposta 2
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            processAnswer(2); // Resposta 3
        }
    }
    
    private void processAnswer(int selectedOption) {
        if (selectedOption == currentQuestion.correctOptionIndex) {
            score += 20;
            System.out.println("Resposta correta! +20 pontos.");
        } else {
            increaseDifficulty();
            System.out.println("Resposta errada! Dificuldade aumentada.");
        }
    
        isQuizActive = false; // Retorna ao jogo
    }

    private void showQuiz() {
        Question randomQuestion = getRandomQuestion();
        if (randomQuestion == null) {
            System.out.println("Sem perguntas disponíveis!");
            return;
        }
        currentQuestion = randomQuestion;
        isQuizActive = true;
    }

    private void checkGameOver() {
        if (life <= 0) {
            System.out.println("Game Over! Reiniciando...");
            resetGame();
        }
    }

    private void resetGame() {
        score = 0;
        life = 3;
        meteorSprites.clear();
        paperSprites.clear();
        isQuizActive = false;
    }


    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        bitmap.dispose();
        batch.dispose();
    }
}