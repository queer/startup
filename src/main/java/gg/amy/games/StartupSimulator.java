package gg.amy.games;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.amy.Bot;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 12/18/17.
 */
@SuppressWarnings({"MethodDoesntCallSuperMethod", "unused", "WeakerAccess"})
public class StartupSimulator extends Game {
    @Getter
    private static final Map<Integer, Card> cards;
    @Getter
    private static final Map<String, String> characterMap;
    private static final int VALUATION_START = 0;
    private static final int VALUATION_GOAL = 1000;
    private static final int VALUATION_MULTIPLIER = 30;
    private static final double POS_VALUATION_MULTIPLIER = 1.1;
    private static final int HAPPINESS_MULTIPLIER = 8;
    private static final int TIME_MULTIPLIER = 1;
    private static final int MAX_WEEKS = 12;
    private static final int TIME_START_VALUE = 10;
    
    static {
        // Load the cards
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final String data = readFullyFromJar("startupsim.json");
            final Collection<Card> tmp = new ArrayList<>(mapper.readValue(data, new TypeReference<List<Card>>() {
            }));
            // Can probably safely reduce to the identity function
            cards = tmp.stream().collect(Collectors.toMap(Card::getId, x -> x));
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
        
        characterMap = new HashMap<String, String>() {{
            put("office-manager", "Office Manager");
            put("backend-dev-01", "Lars, backend");
            put("cfo", "Sheryl, CFO");
            put("chad-marketing", "Chad, marketing");
            put("designer-01", "Clive, designer");
            put("designer-02", "Helga, designer");
            put("journalist", "Journalist");
            put("product-manager", "Eric, product manager");
            put("roy-sales", "Roy, sales");
            put("richard-branson", "Dick, angel");
            put("seo-manager", "Stella, growth hacker");
            put("support-01", "Bubba, support");
            put("tanya-frontend", "Tanya, developer");
            put("vc-fred", "Fred, investor");
            put("vc-mike", "Mike, investor");
            put("victor-frontend", "Victor, developer");
            put("assistant", "Kevin, assistant");
            put("vc-raiden", "Raiden, mentor");
            put("bad-consequence", "Uh oh..");
            put("good-consequence", "$$");
            put("rapper-01", "Dennis, accountant");
            put("rapper-02", "D3nni$, rapper");
            put("rapper-03", "D3nni$, rapper");
            put("twitter-bird", "Twitter bird");
            put("season-01", "Time is moving on..");
            put("season-02", "Winter is coming");
            put("UNKNOWN", "NOT IMPLEMENTED");
        }};
    }
    
    private final Random random = new Random();
    private GameState state;
    
    public StartupSimulator(final Bot bot) {
        super(bot, "Startup Simulator");
    }
    
    public static String readFullyFromJar(@SuppressWarnings("SameParameterValue") final String fileName) {
        final InputStream in = Bot.class.getResourceAsStream("/startupsim.json");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return String.join(" ", reader.lines().collect(Collectors.toList()));
    }
    
    private void sendCard(final MessageReceivedEvent event, final Card card) {
        state.card = card;
        final String[] characterNames = card.getCharacter().split(",");
        final StringBuilder names = new StringBuilder();
        for(String name : characterNames) {
            name = name.trim();
            if(characterMap.containsKey(name)) {
                names.append('*').append(characterMap.get(name)).append('*').append('\n');
            }
        }
        final EmbedBuilder builder = new EmbedBuilder();
        
        final StringBuilder choices = new StringBuilder();
        choices.append("**1**. ").append(card.getChoices().getA().getLabel());
        if(!card.getChoices().getA().getLabel().equalsIgnoreCase(card.getChoices().getB().getLabel())) {
            choices.append('\n').append("**2.** ").append(card.getChoices().getB().getLabel());
        }
        
        builder.addField(getTitle(event), names + "\n" + card.getDescription(), false)
                .addField("", choices.toString(), false)
                .addField("Stats", String.format("Valuation: %s\nHappiness: %s\nMonth: %s %s", formatValuation(),
                        formatHappiness(), getMonth(), monthProgressBar()), false);
        event.getChannel().sendMessage(builder.build()).queue();
    }
    
    private String formatHappiness() {
        if(state.happiness < 0) {
            return ":skull:";
        } else {
            final int happy = Math.floorDiv(state.happiness, 10) + 1;
            if(happy >= 10) {
                return ":smiley:";
            } else if(happy == 9) {
                return ":smile:";
            } else if(happy == 8) {
                return ":sweat_smile:";
            } else if(happy == 7) {
                return ":slight_smile:";
            } else if(happy == 6) {
                return ":neutral_face:";
            } else if(happy == 5) {
                return ":slight_frown:";
            } else if(happy == 4) {
                return ":frowning:";
            } else if(happy == 3) {
                return ":frowning2:";
            } else if(happy == 2) {
                return ":cold_sweat:";
            } else if(happy == 1) {
                return ":angry:";
            } else {
                return ":skull:";
            }
        }
    }
    
    private String formatValuation() {
        if(state.valuation <= 0) {
            return "Worthless";
        }
        if(state.valuation < 1000) {
            return String.format("$%d million", state.valuation);
        } else {
            return String.format("$%.3f billion", state.valuation / 1000D);
        }
    }
    
    private String monthProgressBar() {
        final int day = 84 - state.time;
        final int nextMonth = ((84 - state.time) / 7 + 1) * 7;
        int daysLeft = nextMonth - day;
        int daysUsed = 7 - daysLeft;
        final StringBuilder s = new StringBuilder("`[");
        for(int i = 0; i < daysUsed; i++) {
            s.append('█');
        }
        for(int i = 0; i < daysLeft; i++) {
            s.append('.');
        }
        s.append("]`");
        return s.toString();
    }
    
    private int timeToMonth() {
        return (84 - state.time) / 7 + 1;
    }
    
    private String getMonth() {
        switch(timeToMonth()) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sept";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "???";
        }
    }
    
    @Override
    public void initGame(final MessageReceivedEvent event) {
        final EmbedBuilder init = new EmbedBuilder();
        init.setTitle(String.format("%s | Startup Simulator", event.getAuthor().getName()))
                .addField("Tutorial",
                        "Your goal is to have a valuation of at least $1 billion by the end of the year\n" +
                                "Use your time wisely!\n" +
                                "Also, make sure to keep your employees happy. :smile:\n\n" +
                                "You can type **1** or **2** to make your choice.", false);
        event.getChannel().sendMessage(init.build()).queue(m -> {
            state = createState();
            sendCard(event, cards.get(196));
        });
    }
    
    @Override
    public void endGame(final MessageReceivedEvent event, final String title, final String field, final String msg) {
        final EmbedBuilder builder = new EmbedBuilder().setTitle(title).addField(field, msg, false);
        event.getChannel().sendMessage(builder.build()).queue();
        getBot().getState().deleteState(event.getGuild(), event.getAuthor());
    }
    
    @Override
    public void handleNextMove(final MessageReceivedEvent event) {
        final String choice = event.getMessage().getContentRaw().split(" ", 2)[0].trim();
        // Do nothing if it's not a choice
        if(!choice.equalsIgnoreCase("1") && !choice.equalsIgnoreCase("2")) {
            return;
        }
        setLastInteraction(System.currentTimeMillis());
        if(state.deck.isEmpty()) {
            endGame(event, getTitle(event), "Game over!", "Completely ran out of options to give you!");
            return;
        }
        // Apply state from previous card
        state.discarded.get(state.discarded.size() - 1).getChoices().setChosen(choice);
        updateState(state.card, choice.replace('1', 'a'));
        
        // Check victory / loss
        if(state.valuation <= 0) {
            endGame(event, getTitle(event, "You lost!"), "Worthless", "Everyone abandons your worthless startup and you become an alcoholic.");
            return;
        }
        if(state.time <= 0) {
            if(state.valuation >= 1000) {
                endGame(event, getTitle(event, "You won!"), String.format("Your startup is worth %.3f BILLION dollars!", state.valuation / 1000D),
                        "You still don't have revenue, but who cares - UNICORN, BABY!");
                return;
            } else if(state.valuation >= 800) {
                endGame(event, getTitle(event, "You lost!"), "Out of time",
                        "You raise a lot of money, but being valued at less than a billion is not sexy, " +
                                "and the tech world soon forgets about your startup.");
                return;
            } else {
                endGame(event, getTitle(event, "You lost!"), "Out of time",
                        "You raised a bit of money, but you're just not cool enough to be a unicorn.");
                return;
            }
        }
        if(state.happiness < 0) {
            final String[] opts = {
                    "Your employees revolt, your startup collapses and you go back to driving for Uber.",
                    "Your employees have left, and with no one by your side, you go back to your mom's basement."
            };
            endGame(event, "You lost!", "Your startup is dead", opts[random.nextInt(opts.length)]);
            return;
        }
        
        // First check for an immediate card
        final Optional<Card> any = state.deck.stream().filter(c -> isImmediate(c) && checkConditions(c)).findAny();
        if(any.isPresent()) {
            // We found an immediate card, send it
            state.deck.remove(any.get());
            state.discarded.add(any.get());
            sendCard(event, any.get());
        } else {
            for(int i = 0; i < Math.min(state.deck.size(), 200); i++) {
                // Try max. 200 times to pick a valid card, otherwise bail
                final Card card = state.deck.get(random.nextInt(state.deck.size()));
                if(!isImmediate(card) && checkConditions(card)) {
                    state.deck.remove(card);
                    state.discarded.add(card);
                    sendCard(event, card);
                    return;
                }
            }
            endGame(event, getTitle(event), "Game over!", "Completely ran out of options to give you!");
        }
    }
    
    private boolean isImmediate(final Card card) {
        return card.conditions.contains("immediate");
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean isInteger(final String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch(final Exception e) {
            return false;
        }
    }
    
    private boolean checkConditions(final Card card) {
        // Conditions may be
        // immediate              - Happens immediately (see #isImmediate(Card) for this)
        // choice(id.choice)      - `choice` must have been chosen on card `id`
        // aboveHappiness(amount) - happiness >= `amount`
        // belowHappiness(amount) - happiness <= `amount`
        // aboveValuation(amount) - valuation >= `amount`
        // belowValuation(amount) - valuation <= `amount`
        // beforeWeek(week)       - week >= `week - 1`
        // afterWeek(week)        - week <= `week - 1`
        // card(id)               - card `id` in discard
        final String conditionStr = card.conditions;
        if(conditionStr.trim().isEmpty()) {
            return true;
        }
        final String[] conditions = conditionStr.split("&");
        final Pattern pattern = Pattern.compile("\\((.+)\\)");
        final Collection<Boolean> checks = new ArrayList<>();
        for(String c : conditions) {
            c = c.trim();
            final String name = c.split("\\(", 2)[0];
            if(c.indexOf('(') != -1) {
                // it has a condition, do regex test
                final Matcher matcher = pattern.matcher(c);
                if(matcher.find()) {
                    final String group = matcher.group(1);
                    if(name.contains("choice")) {
                        final String[] split = group.split("\\.");
                        final int cardId = Integer.parseInt(split[0]);
                        final String choice = split[1];
                        final Optional<Card> first = state.discarded.stream().filter(e -> e.getId() == cardId).findFirst();
                        if(first.isPresent()) {
                            if(first.get().getChoices().getChosen() != null) {
                                final String chosen = first.get().getChoices().getChosen()
                                        .replace("1", "a").replace("2", "b");
                                if(chosen.equalsIgnoreCase(choice)) {
                                    checks.add(true);
                                } else {
                                    checks.add(false);
                                }
                            } else {
                                checks.add(false);
                            }
                        } else {
                            checks.add(false);
                        }
                    } else if(isInteger(group)) {
                        // Check values
                        final int value = Integer.parseInt(group);
                        switch(name) {
                            case "aboveHappiness":
                                checks.add(state.happiness >= value);
                                break;
                            case "belowHappiness":
                                checks.add(state.happiness <= value);
                                break;
                            case "aboveValuation":
                                checks.add(state.valuation >= value);
                                break;
                            case "belowValuation":
                                checks.add(state.valuation <= value);
                                break;
                            case "afterWeek":
                                checks.add(timeToMonth() >= value);
                                break;
                            case "beforeWeek":
                                checks.add(timeToMonth() <= value);
                                break;
                            case "card":
                                checks.add(state.happiness >= value);
                                break;
                        }
                    }
                }
            }
        }
        return !checks.contains(false);
    }
    
    private void updateState(final Card card, final String choice) {
        // Initial card
        if(card.id == 196) {
            state.valuation = 250;
            return;
        }
        // All other cards
        final Choice c = choice.equalsIgnoreCase("a") ? card.choices.a : card.choices.b;
        state.happiness += c.values.happiness * HAPPINESS_MULTIPLIER;
        state.valuation += c.values.valuation * VALUATION_MULTIPLIER;
        state.time += c.values.time * TIME_MULTIPLIER;
    }
    
    private GameState createState() {
        final GameState state = new GameState();
        final List<Card> values = new ArrayList<>();
        cards.values().forEach(e -> values.add((Card) e.clone()));
        final Optional<Card> first = values.stream().filter(e -> e.getId() == 196).findFirst();
        //noinspection ConstantConditions
        state.discarded.add(first.get());
        state.deck.addAll(values.stream().filter(e -> e.getId() != 196).collect(Collectors.toList()));
        state.card = first.get();
        state.time = 84;
        state.happiness = 50;
        return state;
    }
    
    @Value
    public static final class Card implements Cloneable {
        private final int id;
        private final String description;
        private final String character;
        private final String conditions;
        private final Choices choices;
        
        @Override
        protected Object clone() {
            return new Card(id, description, character, conditions, (Choices) choices.clone());
        }
    }
    
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public static final class Choices implements Cloneable {
        @Getter
        @Setter
        private Choice a;
        @Getter
        @Setter
        private Choice b;
        @Getter
        @Setter
        private String chosen;
        
        public Choices(final Choice a, final Choice b) {
            this.a = a;
            this.b = b;
        }
        
        public Choices() {
        }
        
        @Override
        protected Object clone() {
            return new Choices((Choice) a.clone(), (Choice) b.clone());
        }
    }
    
    @Value
    public static final class Choice implements Cloneable {
        private final String label;
        private final Values values;
        
        @Override
        protected Object clone() {
            return new Choice(label, (Values) values.clone());
        }
        
        @SuppressWarnings("InnerClassTooDeeplyNested")
        @Value
        public static final class Values implements Cloneable {
            private final int valuation;
            private final int happiness;
            private final int time;
            
            @Override
            protected Object clone() {
                return new Values(valuation, happiness, time);
            }
        }
    }
    
    public static final class GameState {
        private final List<Card> deck = new ArrayList<>();
        private final List<Card> discarded = new ArrayList<>();
        private Card card;
        private int valuation;
        private int happiness;
        private int time;
        private boolean win;
    }
}
