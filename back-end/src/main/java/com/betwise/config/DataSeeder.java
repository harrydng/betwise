package com.betwise.config;

import com.betwise.model.*;
import com.betwise.repository.*;
import com.betwise.util.ChoiceListCodec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fills an empty database with assets, lessons, quizzes and news on
 * startup. Each group is only inserted when its table is empty, so this
 * never overwrites or duplicates existing data.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AssetRepository assetRepository;
    private final LessonCategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final NewsArticleRepository newsRepository;
    private final LessonCheckpointRepository checkpointRepository;

    public DataSeeder(
            AssetRepository assetRepository,
            LessonCategoryRepository categoryRepository,
            LessonRepository lessonRepository,
            QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            NewsArticleRepository newsRepository,
            LessonCheckpointRepository checkpointRepository) {

        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.newsRepository = newsRepository;
        this.checkpointRepository = checkpointRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedAssets();
        seedLessons();
        seedCheckpoints();
        seedNews();
    }

    // MARK: - Assets
    //
    // Every ticker here has a price in MockMarketPriceService.

    private void seedAssets() {
        if (assetRepository.count() > 0) {
            return;
        }

        assetRepository.saveAll(List.of(
                new Asset("AAPL", "Apple", Asset.AssetType.STOCK),
                new Asset("NVDA", "NVIDIA", Asset.AssetType.STOCK),
                new Asset("MSFT", "Microsoft", Asset.AssetType.STOCK),
                new Asset("VOO", "Vanguard S&P 500 ETF", Asset.AssetType.ETF),
                new Asset("TSLA", "Tesla", Asset.AssetType.STOCK),
                new Asset("AMZN", "Amazon", Asset.AssetType.STOCK),
                new Asset("GOOGL", "Alphabet", Asset.AssetType.STOCK),
                new Asset("QQQ", "Invesco QQQ Trust", Asset.AssetType.ETF),
                new Asset("BND", "Vanguard Total Bond Market", Asset.AssetType.BOND),
                new Asset("TLT", "iShares 20+ Year Treasury", Asset.AssetType.BOND),
                new Asset("O", "Realty Income", Asset.AssetType.REIT),
                new Asset("VNQ", "Vanguard Real Estate ETF", Asset.AssetType.REIT)
        ));

        log.info("Seeded {} assets", assetRepository.count());
    }

    // MARK: - Lessons and quizzes

    private void seedLessons() {
        if (categoryRepository.count() > 0 || lessonRepository.count() > 0) {
            return;
        }

        Map<String, LessonCategory> categories = categoryRepository.saveAll(List.of(
                new LessonCategory("Investing Basics", "basics",
                        "Start here: what stocks and bonds are and how they make money."),
                new LessonCategory("Stocks & Funds", "stocks-funds",
                        "How funds bundle many investments into one."),
                new LessonCategory("Managing Risk", "risk",
                        "Protect your portfolio by understanding and spreading risk."),
                new LessonCategory("Advanced", "advanced",
                        "Analysis techniques and strategies for experienced investors.")
        )).stream().collect(Collectors.toMap(LessonCategory::getSlug, category -> category));

        Lesson stocks = lesson(categories.get("basics"), "What Is a Stock?",
                "Learn what it means to own a slice of a company, why share prices move, and how investors make money from stocks.",
                Lesson.Topic.STOCKS, Lesson.Difficulty.BEGINNER, "50");
        Lesson bonds = lesson(categories.get("basics"), "Bonds 101",
                "Discover how lending money to governments and companies pays interest, and why bonds can steady a portfolio.",
                Lesson.Topic.BONDS, Lesson.Difficulty.BEGINNER, "50");
        Lesson etfs = lesson(categories.get("stocks-funds"), "How ETFs Work",
                "See how a single fund can hold hundreds of companies, and why low-cost index ETFs are a favorite of long-term investors.",
                Lesson.Topic.ETFS, Lesson.Difficulty.BEGINNER, "50");
        Lesson diversification = lesson(categories.get("risk"), "The Power of Diversification",
                "Find out why spreading money across many investments lowers risk without giving up much return.",
                Lesson.Topic.DIVERSIFICATION, Lesson.Difficulty.BEGINNER, "75");
        Lesson risk = lesson(categories.get("risk"), "Understanding Risk & Return",
                "Learn how volatility, time horizon, and your own goals shape how much risk makes sense for you.",
                Lesson.Topic.RISK, Lesson.Difficulty.INTERMEDIATE, "75");
        Lesson realEstate = lesson(categories.get("stocks-funds"), "Investing in Real Estate",
                "Explore REITs, a way to earn rental income from offices, apartments, and warehouses without buying property.",
                Lesson.Topic.REAL_ESTATE, Lesson.Difficulty.INTERMEDIATE, "75");
        Lesson balanceSheet = lesson(categories.get("advanced"), "Reading a Balance Sheet",
                "Learn to read a company's assets, debts, and equity to judge whether it's on solid ground.",
                Lesson.Topic.FUNDAMENTAL_ANALYSIS, Lesson.Difficulty.INTERMEDIATE, "100");
        Lesson charts = lesson(categories.get("advanced"), "Chart Patterns & Trends",
                "Spot support, resistance, and moving averages, and learn where technical analysis helps and where it doesn't.",
                Lesson.Topic.TECHNICAL_ANALYSIS, Lesson.Difficulty.ADVANCED, "100");
        Lesson shorting = lesson(categories.get("advanced"), "Short Selling Explained",
                "Understand how investors profit when prices fall, and why the risk of a short position has no ceiling.",
                Lesson.Topic.SHORT_SELLING, Lesson.Difficulty.ADVANCED, "125");

        List<Lesson> lessons = List.of(
                stocks, bonds, etfs, diversification, risk, realEstate, balanceSheet, charts, shorting);

        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setPosition(i + 1);
            lessons.get(i).setPublished(true);
        }

        lessonRepository.saveAll(lessons);

        quiz(stocks, "Stock Basics Check",
                question("What do you own when you buy a share of stock?",
                        "A small piece of the company",
                        "A small piece of the company", "A loan to the company", "A product the company makes"),
                question("Which of these can make you money from a stock?",
                        "Price going up and dividends",
                        "Price going up and dividends", "Only price going down", "Interest payments"));

        quiz(bonds, "Bonds Check",
                question("When you buy a bond, you are…",
                        "Lending money to the issuer",
                        "Buying part of the company", "Lending money to the issuer", "Opening a savings account"),
                question("What usually happens to existing bond prices when interest rates rise?",
                        "They fall",
                        "They rise", "They fall", "They stay the same"));

        quiz(etfs, "ETF Check",
                question("What is an ETF?",
                        "A fund that holds many investments and trades like a stock",
                        "A single company's stock", "A fund that holds many investments and trades like a stock", "A type of bank account"),
                question("Why do many long-term investors like index ETFs?",
                        "Low fees and broad diversification",
                        "Guaranteed returns", "Low fees and broad diversification", "They never lose value"));

        quiz(diversification, "Diversification Check",
                question("Diversification mainly helps you…",
                        "Reduce the impact of any one investment doing badly",
                        "Guarantee a profit", "Reduce the impact of any one investment doing badly", "Avoid all taxes"),
                question("Which portfolio is most diversified?",
                        "A mix of stocks, bonds and funds across industries",
                        "All your money in one stock", "Three tech stocks", "A mix of stocks, bonds and funds across industries"));

        quiz(risk, "Risk & Return Check",
                question("In general, investments with higher potential returns…",
                        "Come with higher risk",
                        "Come with higher risk", "Are always safer", "Have no ups and downs"),
                question("Why does a long time horizon let you take more risk?",
                        "You have time to recover from downturns",
                        "Markets never fall over long periods", "You have time to recover from downturns", "Fees disappear over time"));

        quiz(realEstate, "Real Estate Check",
                question("What does a REIT do?",
                        "Owns income-producing real estate and pays out most of its income",
                        "Lends money to home buyers only", "Owns income-producing real estate and pays out most of its income", "Builds houses to sell once"),
                question("A key benefit of REITs compared to buying property directly is…",
                        "You can invest with a small amount and sell easily",
                        "They never lose value", "You can invest with a small amount and sell easily", "You get to live in the property"));

        quiz(balanceSheet, "Balance Sheet Check",
                question("What is the balance sheet equation?",
                        "Assets = Liabilities + Equity",
                        "Assets = Liabilities + Equity", "Revenue = Profit + Costs", "Equity = Assets + Liabilities"),
                question("A company with far more debt than assets is generally…",
                        "Riskier",
                        "Safer", "Riskier", "Guaranteed to grow"));

        quiz(charts, "Chart Patterns Check",
                question("A 'support' level is a price where…",
                        "Buying has tended to stop a fall",
                        "Selling has tended to stop a rise", "Buying has tended to stop a fall", "The stock is delisted"),
                question("A moving average is used to…",
                        "Smooth out price movements to show the trend",
                        "Predict prices exactly", "Smooth out price movements to show the trend", "Calculate dividends"));

        quiz(shorting, "Short Selling Check",
                question("A short seller profits when the price…",
                        "Falls",
                        "Rises", "Falls", "Stays exactly the same"),
                question("Why is short selling considered risky?",
                        "Losses have no upper limit",
                        "Losses have no upper limit", "It's illegal", "Gains have no upper limit"));

        log.info("Seeded {} lesson categories, {} lessons and {} quizzes",
                categoryRepository.count(), lessonRepository.count(), quizRepository.count());
    }

    private static Lesson lesson(
            LessonCategory category,
            String title,
            String description,
            Lesson.Topic topic,
            Lesson.Difficulty difficulty,
            String cashReward) {

        Lesson lesson = new Lesson();
        lesson.setCategory(category);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setTopic(topic);
        lesson.setDifficulty(difficulty);
        lesson.setCashReward(new BigDecimal(cashReward));
        return lesson;
    }

    private record QuestionSeed(String question, String correctAnswer, List<String> choices) {
    }

    private static QuestionSeed question(String question, String correctAnswer, String... choices) {
        return new QuestionSeed(question, correctAnswer, List.of(choices));
    }

    /** Saves a quiz that unlocks after the lesson's first checkpoint. */
    private void quiz(Lesson lesson, String title, QuestionSeed... questions) {

        Quiz quiz = quizRepository.save(new Quiz(lesson, title, 1));

        for (int i = 0; i < questions.length; i++) {
            QuestionSeed seed = questions[i];

            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setQuestion(seed.question());
            question.setChoices(ChoiceListCodec.encode(seed.choices()));
            question.setCorrectAnswer(seed.correctAnswer());
            question.setQuestionOrder(i + 1);
            question.setCashReward(BigDecimal.ZERO);

            questionRepository.save(question);
        }
    }

    // MARK: - Lesson checkpoints
    //
    // Runs on every startup and fills in sections for any seeded lesson that
    // has none, including lessons seeded before checkpoints existed. Lessons
    // created in the admin app aren't in this map and are left alone.

    private void seedCheckpoints() {
        int added = 0;

        for (Lesson lesson : lessonRepository.findAll()) {
            List<String[]> sections = CHECKPOINTS.get(lesson.getTitle());

            if (sections == null || checkpointRepository.countByLessonId(lesson.getId()) > 0) {
                continue;
            }

            for (int i = 0; i < sections.size(); i++) {
                checkpointRepository.save(new LessonCheckpoint(
                        lesson, i + 1, sections.get(i)[0], sections.get(i)[1]));
                added++;
            }
        }

        if (added > 0) {
            log.info("Seeded {} lesson checkpoints", added);
        }
    }

    private static final Map<String, List<String[]>> CHECKPOINTS = Map.of(
            "What Is a Stock?", List.of(
                    new String[]{"Owning a slice",
                            "A **stock** (or share) is a small piece of ownership in a company. If a company is split into 1,000,000 shares and you own 1,000, you own 0.1% of it.\n\nAs an owner you share in the company's success, and its setbacks."},
                    new String[]{"Why prices move",
                            "A share's price is whatever buyers are willing to pay right now. It rises when people expect the company to earn more in the future, and falls when they expect less.\n\n- Earnings reports\n- News about products or leaders\n- The economy as a whole"},
                    new String[]{"How you make money",
                            "Stocks pay off in two ways:\n\n- **Price growth:** you sell for more than you paid.\n- **Dividends:** some companies share part of their profits with owners in cash.\n\nNeither is guaranteed, which is why stocks carry risk."}),
            "Bonds 101", List.of(
                    new String[]{"Lending, not owning",
                            "When you buy a **bond**, you lend money to a government or company. In return they promise to pay you interest and give your money back on a set date, called *maturity*."},
                    new String[]{"Interest and price",
                            "Bonds pay a fixed interest rate. If new bonds start paying more, older bonds paying less become less attractive, so their price falls.\n\nThat's why bond prices usually **fall when interest rates rise**."},
                    new String[]{"Why bonds steady a portfolio",
                            "Bonds tend to move less than stocks, and often hold up when stocks fall. Mixing them in can make your portfolio's ups and downs smaller."}),
            "How ETFs Work", List.of(
                    new String[]{"A basket in one trade",
                            "An **ETF** (exchange-traded fund) holds many investments at once, like every company in the S&P 500, but trades like a single stock."},
                    new String[]{"Index funds",
                            "Many ETFs simply copy an index. Instead of trying to beat the market, they aim to *be* the market, which keeps costs low."},
                    new String[]{"Fees matter",
                            "ETFs charge a yearly fee called the **expense ratio**. A 0.03% fee costs $3 a year on $10,000. Over decades, low fees leave noticeably more money in your pocket."}),
            "The Power of Diversification", List.of(
                    new String[]{"Don't bet on one horse",
                            "If all your money is in one company and it stumbles, your whole portfolio stumbles. **Diversification** spreads money across many investments so no single one can sink you."},
                    new String[]{"Spread across kinds",
                            "Diversify in more than one way:\n\n- Many companies\n- Different industries\n- Different asset types, like stocks, bonds and real estate"},
                    new String[]{"What it can't do",
                            "Diversification lowers the risk of any one investment, but it can't remove market risk. When the whole market falls, most investments fall with it."}),
            "Understanding Risk & Return", List.of(
                    new String[]{"The trade-off",
                            "Investments that can grow more usually swing more. Higher potential **return** almost always comes with higher **risk**."},
                    new String[]{"Time is on your side",
                            "Short-term prices are unpredictable, but over many years markets have tended to rise. A long time horizon gives you time to recover from downturns."},
                    new String[]{"Know yourself",
                            "The right amount of risk depends on your goals, when you'll need the money, and how you'd feel watching your portfolio drop 20%. Be honest with yourself."}),
            "Investing in Real Estate", List.of(
                    new String[]{"What a REIT is",
                            "A **REIT** (real estate investment trust) owns income-producing property like apartments, offices or warehouses, and is required to pay out most of its income to shareholders."},
                    new String[]{"Why investors use them",
                            "REITs let you invest in property with a small amount of money, and sell any trading day. No landlord duties required."},
                    new String[]{"What moves them",
                            "REITs are sensitive to interest rates. Higher rates make borrowing pricier and their dividends less attractive compared to bonds."}),
            "Reading a Balance Sheet", List.of(
                    new String[]{"The basic equation",
                            "Every balance sheet follows one rule:\n\n**Assets = Liabilities + Equity**\n\nWhat a company owns equals what it owes plus what belongs to its owners."},
                    new String[]{"Assets and liabilities",
                            "- **Assets:** cash, inventory, buildings, patents\n- **Liabilities:** loans, bills and other debts\n\nA company with far more debt than assets is on shaky ground."},
                    new String[]{"Using it",
                            "Compare a company to its peers. Is its debt growing faster than its assets? Does it have enough cash to cover its short-term bills? These questions reveal financial health."}),
            "Chart Patterns & Trends", List.of(
                    new String[]{"Support and resistance",
                            "**Support** is a price where buying has tended to stop a fall. **Resistance** is a price where selling has tended to stop a rise. Traders watch whether price breaks through them."},
                    new String[]{"Moving averages",
                            "A **moving average** smooths out daily noise by averaging recent prices. When price stays above a rising average, the trend is generally up."},
                    new String[]{"Know the limits",
                            "Charts show what happened, not what will happen. Technical analysis is one tool among many, never a guarantee."}),
            "Short Selling Explained", List.of(
                    new String[]{"Betting on a fall",
                            "**Short selling** means borrowing shares, selling them, and hoping to buy them back cheaper later. The difference is your profit."},
                    new String[]{"Unlimited risk",
                            "If you buy a stock, the most you can lose is what you paid. If you short one, the price can keep rising, so **losses have no upper limit**."},
                    new String[]{"Why people short",
                            "Investors short to profit from a decline or to hedge other positions. It's an advanced strategy best left until you fully understand the risks."})
    );

    // MARK: - News

    private void seedNews() {
        if (newsRepository.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        newsRepository.saveAll(List.of(
                new NewsArticle(NewsArticle.Category.MARKETS,
                        "Tech stocks lead a broad rally as chipmakers hit record highs",
                        "Semiconductor shares climbed for a third straight session as investors bet that demand for AI hardware will keep growing into next year. The Nasdaq outpaced the other major indexes.",
                        "Betwise Markets", "NVDA,QQQ", now.minusMinutes(18)),
                new NewsArticle(NewsArticle.Category.ECONOMY,
                        "Inflation cools for a second month, easing pressure on rates",
                        "Consumer prices rose less than expected last month. Economists say a slower pace gives the central bank room to hold rates steady, which tends to support both stocks and bonds.",
                        "Betwise Economy", "BND,TLT", now.minusMinutes(54)),
                new NewsArticle(NewsArticle.Category.EARNINGS,
                        "Apple beats estimates on strong services growth",
                        "Revenue from the App Store, iCloud, and other services reached a new high, offsetting slower hardware sales. Shares rose in after-hours trading.",
                        "Betwise Earnings", "AAPL", now.minusMinutes(130)),
                new NewsArticle(NewsArticle.Category.TECH,
                        "Cloud spending accelerates as companies expand AI projects",
                        "Microsoft, Amazon, and Alphabet all reported faster growth in their cloud businesses, a sign that companies are moving AI experiments into production.",
                        "Betwise Tech", "MSFT,AMZN,GOOGL", now.minusMinutes(205)),
                new NewsArticle(NewsArticle.Category.MARKETS,
                        "Real estate funds rebound as mortgage rates dip",
                        "REITs gained after a drop in long-term interest rates. Lower rates make property financing cheaper and dividend-paying real estate more attractive.",
                        "Betwise Markets", "VNQ,O", now.minusMinutes(320)),
                new NewsArticle(NewsArticle.Category.EARNINGS,
                        "Tesla deliveries fall short as price cuts weigh on margins",
                        "The electric-vehicle maker delivered fewer cars than analysts expected. Shares slipped as investors weighed slower growth against new model launches.",
                        "Betwise Earnings", "TSLA", now.minusMinutes(470))
        ));

        log.info("Seeded {} news articles", newsRepository.count());
    }
}
