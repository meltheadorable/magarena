package magic.ai;

import magic.model.MagicGame;
import magic.model.MagicPlayer;
import magic.model.MagicRandom;
import magic.model.MagicGameLog;
import magic.model.event.MagicEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class VegasAI implements MagicAI {

    private static final long SEC_TO_NANO=1000000000L;
    private static final int THREADS=Runtime.getRuntime().availableProcessors();
    
    private final boolean LOGGING = Boolean.getBoolean("debug");
    private final boolean CHEAT;
    
    VegasAI(final boolean cheat) {
        CHEAT = cheat;
    }
    
    private void log(final String message) {
        MagicGameLog.log(message);
        if (LOGGING) {
            System.err.println(message);
        }
    }
    
    @Override
    public Object[] findNextEventChoiceResults(final MagicGame sourceGame,final MagicPlayer scorePlayer) {
        final long startTime = System.currentTimeMillis();

        final MagicGame choiceGame=new MagicGame(sourceGame,scorePlayer);
        if (!CHEAT) {
            choiceGame.hideHiddenCards();
        }
        final MagicEvent event=choiceGame.getNextEvent();
        final List<Object[]> choiceResultsList=event.getArtificialChoiceResults(choiceGame);
        
        // No choices
        final int size=choiceResultsList.size();
        if (size==0) {
            throw new RuntimeException("No choice results");
        }        
        
        // Single choice        
        if (size==1) {
            return sourceGame.map(choiceResultsList.get(0));
        }
        
        // Multiple choices
        final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        final List<VegasScore> scores=new ArrayList<VegasScore>();
        final int artificialLevel = sourceGame.getArtificialLevel(scorePlayer.getIndex());
        final int rounds = (size + THREADS - 1) / THREADS;
        final long slice = artificialLevel * SEC_TO_NANO / rounds;
        for (final Object[] choiceResults : choiceResultsList) {
            final VegasScore score=new VegasScore(choiceResults);
            scores.add(score);
            executor.execute(new VegasWorker(
                CHEAT,
                choiceGame,
                score,
                new Random(MagicRandom.nextInt(1000000)),
                slice
            ));
        }
        executor.shutdown();
        try { //await termination
            executor.awaitTermination(artificialLevel + 1,TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            // force termination of workers
            executor.shutdownNow();
        }
        
        // Return best choice
        VegasScore bestScore=scores.get(0);
        for (final VegasScore score : scores) {
            if (score.getScore() > bestScore.getScore()) {
                bestScore = score;
            }
        }
        
        // Logging.
        final long timeTaken = System.currentTimeMillis() - startTime;
        log("VEGAS" +
            " cheat=" + CHEAT +
            " index=" + scorePlayer.getIndex() +
            " life=" + scorePlayer.getLife() +
            " phase=" + sourceGame.getPhase().getType() + 
            " slice=" + (slice/1000000) +
            " time=" + timeTaken
            );
        for (final VegasScore score : scores) {
            log((score == bestScore ? "* " : "  ") + score);
        }

        return sourceGame.map(bestScore.getChoiceResults());
    }
}
