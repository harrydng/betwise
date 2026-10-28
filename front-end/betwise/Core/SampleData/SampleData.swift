// Core/SampleData/SampleData.swift

import Foundation

/// Data the app shows until the user signs in, and in previews. Built from
/// the same models the API returns, so screens don't care where data came
/// from.
enum SampleData {

    // MARK: - Market

    static let assets: [Asset] = [
        Asset(id: 1, ticker: "AAPL", name: "Apple", assetType: .stock, currentPrice: decimal("200.00")),
        Asset(id: 2, ticker: "NVDA", name: "NVIDIA", assetType: .stock, currentPrice: decimal("180.00")),
        Asset(id: 3, ticker: "MSFT", name: "Microsoft", assetType: .stock, currentPrice: decimal("420.00")),
        Asset(id: 4, ticker: "VOO", name: "Vanguard S&P 500 ETF", assetType: .etf, currentPrice: decimal("550.00")),
        Asset(id: 5, ticker: "TSLA", name: "Tesla", assetType: .stock, currentPrice: decimal("238.45")),
        Asset(id: 6, ticker: "AMZN", name: "Amazon", assetType: .stock, currentPrice: decimal("186.30")),
        Asset(id: 7, ticker: "GOOGL", name: "Alphabet", assetType: .stock, currentPrice: decimal("171.12")),
        Asset(id: 8, ticker: "QQQ", name: "Invesco QQQ Trust", assetType: .etf, currentPrice: decimal("482.60")),
        Asset(id: 9, ticker: "BND", name: "Vanguard Total Bond Market", assetType: .bond, currentPrice: decimal("72.84")),
        Asset(id: 10, ticker: "TLT", name: "iShares 20+ Year Treasury", assetType: .bond, currentPrice: decimal("91.15")),
        Asset(id: 11, ticker: "O", name: "Realty Income", assetType: .reit, currentPrice: decimal("57.21")),
        Asset(id: 12, ticker: "VNQ", name: "Vanguard Real Estate ETF", assetType: .reit, currentPrice: decimal("89.40"))
    ]

    private static let dayChanges: [String: Double] = [
        "AAPL": 1.24, "NVDA": 3.42, "MSFT": 0.58, "VOO": 0.41,
        "TSLA": -2.17, "AMZN": 0.92, "GOOGL": -0.36, "QQQ": 0.77,
        "BND": 0.08, "TLT": -0.31, "O": -0.44, "VNQ": 0.21
    ]

    /// Today's move for a ticker. Tickers without sample data get a
    /// stable made-up move so live assets still have a chart.
    static func dayChange(for ticker: String) -> Double {
        if let change = dayChanges[ticker] {
            return change
        }

        var generator = SeededGenerator(seed: stableHash(ticker))
        return Double.random(in: -2.5...2.5, using: &generator)
    }

    static let watchlistAssetIDs: Set<Int> = [2, 3, 8, 5]

    static let indices: [MarketIndex] = [
        makeIndex(id: "SPX", name: "S&P 500", value: 5_842.17, change: 0.61),
        makeIndex(id: "IXIC", name: "Nasdaq", value: 18_573.13, change: 0.94),
        makeIndex(id: "DJI", name: "Dow Jones", value: 42_863.86, change: 0.32),
        makeIndex(id: "RUT", name: "Russell 2000", value: 2_231.50, change: -0.18)
    ]

    // MARK: - Portfolio

    static let startingBalance = decimal("10000.00")

    static let portfolio: Portfolio = makePortfolio()

    static let transactions: [Transaction] = [
        makeTransaction(id: 5, ticker: "NVDA", type: .buy, quantity: 5, price: "164.80", daysAgo: 2),
        makeTransaction(id: 4, ticker: "TSLA", type: .sell, quantity: 1, price: "246.10", daysAgo: 6),
        makeTransaction(id: 3, ticker: "MSFT", type: .buy, quantity: 3, price: "398.10", daysAgo: 13),
        makeTransaction(id: 2, ticker: "VOO", type: .buy, quantity: 4, price: "512.75", daysAgo: 21),
        makeTransaction(id: 1, ticker: "AAPL", type: .buy, quantity: 12, price: "182.40", daysAgo: 34)
    ]

    // MARK: - Lessons

    static let lessonCategories: [LessonCategory] = [
        LessonCategory(id: 1, name: "Investing Basics", slug: "basics", description: nil, createdAt: .now),
        LessonCategory(id: 2, name: "Stocks & Funds", slug: "stocks-funds", description: nil, createdAt: .now),
        LessonCategory(id: 3, name: "Managing Risk", slug: "risk", description: nil, createdAt: .now),
        LessonCategory(id: 4, name: "Advanced", slug: "advanced", description: nil, createdAt: .now)
    ]

    static let lessons: [Lesson] = [
        Lesson(
            id: 1, title: "What Is a Stock?",
            description: "Learn what it means to own a slice of a company, why share prices move, and how investors make money from stocks.",
            category: "Investing Basics", categorySlug: "basics",
            topic: .stocks, difficulty: .beginner, cashReward: 50, checkpointCount: 3
        ),
        Lesson(
            id: 2, title: "Bonds 101",
            description: "Discover how lending money to governments and companies pays interest, and why bonds can steady a portfolio.",
            category: "Investing Basics", categorySlug: "basics",
            topic: .bonds, difficulty: .beginner, cashReward: 50, checkpointCount: 3
        ),
        Lesson(
            id: 3, title: "How ETFs Work",
            description: "See how a single fund can hold hundreds of companies, and why low-cost index ETFs are a favorite of long-term investors.",
            category: "Stocks & Funds", categorySlug: "stocks-funds",
            topic: .etfs, difficulty: .beginner, cashReward: 50, checkpointCount: 3
        ),
        Lesson(
            id: 4, title: "The Power of Diversification",
            description: "Find out why spreading money across many investments lowers risk without giving up much return.",
            category: "Managing Risk", categorySlug: "risk",
            topic: .diversification, difficulty: .beginner, cashReward: 75, checkpointCount: 3
        ),
        Lesson(
            id: 5, title: "Understanding Risk & Return",
            description: "Learn how volatility, time horizon, and your own goals shape how much risk makes sense for you.",
            category: "Managing Risk", categorySlug: "risk",
            topic: .risk, difficulty: .intermediate, cashReward: 75, checkpointCount: 3
        ),
        Lesson(
            id: 6, title: "Investing in Real Estate",
            description: "Explore REITs, a way to earn rental income from offices, apartments, and warehouses without buying property.",
            category: "Stocks & Funds", categorySlug: "stocks-funds",
            topic: .realEstate, difficulty: .intermediate, cashReward: 75, checkpointCount: 3
        ),
        Lesson(
            id: 7, title: "Reading a Balance Sheet",
            description: "Learn to read a company's assets, debts, and equity to judge whether it's on solid ground.",
            category: "Advanced", categorySlug: "advanced",
            topic: .fundamentalAnalysis, difficulty: .intermediate, cashReward: 100, checkpointCount: 3
        ),
        Lesson(
            id: 8, title: "Chart Patterns & Trends",
            description: "Spot support, resistance, and moving averages, and learn where technical analysis helps and where it doesn't.",
            category: "Advanced", categorySlug: "advanced",
            topic: .technicalAnalysis, difficulty: .advanced, cashReward: 100, checkpointCount: 3
        ),
        Lesson(
            id: 9, title: "Short Selling Explained",
            description: "Understand how investors profit when prices fall, and why the risk of a short position has no ceiling.",
            category: "Advanced", categorySlug: "advanced",
            topic: .shortSelling, difficulty: .advanced, cashReward: 125, checkpointCount: 3
        )
    ]

    static let lessonProgress: [Int: LessonProgress] = [
        1: makeProgress(lessonID: 1, completed: true, checkpoint: 3, completedCheckpoints: 3, daysAgo: 20),
        2: makeProgress(lessonID: 2, completed: true, checkpoint: 3, completedCheckpoints: 3, daysAgo: 12),
        3: makeProgress(lessonID: 3, completed: true, checkpoint: 3, completedCheckpoints: 3, daysAgo: 7),
        4: makeProgress(lessonID: 4, completed: false, checkpoint: 2, completedCheckpoints: 1, daysAgo: 1)
    ]

    // MARK: - News

    static let news: [NewsArticle] = [
        NewsArticle(
            id: 1, category: .markets,
            headline: "Tech stocks lead a broad rally as chipmakers hit record highs",
            summary: "Semiconductor shares climbed for a third straight session as investors bet that demand for AI hardware will keep growing into next year. The Nasdaq outpaced the other major indexes.",
            source: "Betwise Markets", publishedAt: minutesAgo(18), tickers: ["NVDA", "QQQ"]
        ),
        NewsArticle(
            id: 2, category: .economy,
            headline: "Inflation cools for a second month, easing pressure on rates",
            summary: "Consumer prices rose less than expected last month. Economists say a slower pace gives the central bank room to hold rates steady, which tends to support both stocks and bonds.",
            source: "Betwise Economy", publishedAt: minutesAgo(54), tickers: ["BND", "TLT"]
        ),
        NewsArticle(
            id: 3, category: .earnings,
            headline: "Apple beats estimates on strong services growth",
            summary: "Revenue from the App Store, iCloud, and other services reached a new high, offsetting slower hardware sales. Shares rose in after-hours trading.",
            source: "Betwise Earnings", publishedAt: minutesAgo(130), tickers: ["AAPL"]
        ),
        NewsArticle(
            id: 4, category: .tech,
            headline: "Cloud spending accelerates as companies expand AI projects",
            summary: "Microsoft, Amazon, and Alphabet all reported faster growth in their cloud businesses, a sign that companies are moving AI experiments into production.",
            source: "Betwise Tech", publishedAt: minutesAgo(205), tickers: ["MSFT", "AMZN", "GOOGL"]
        ),
        NewsArticle(
            id: 5, category: .markets,
            headline: "Real estate funds rebound as mortgage rates dip",
            summary: "REITs gained after a drop in long-term interest rates. Lower rates make property financing cheaper and dividend-paying real estate more attractive.",
            source: "Betwise Markets", publishedAt: minutesAgo(320), tickers: ["VNQ", "O"]
        ),
        NewsArticle(
            id: 6, category: .earnings,
            headline: "Tesla deliveries fall short as price cuts weigh on margins",
            summary: "The electric-vehicle maker delivered fewer cars than analysts expected. Shares slipped as investors weighed slower growth against new model launches.",
            source: "Betwise Earnings", publishedAt: minutesAgo(470), tickers: ["TSLA"]
        )
    ]

    // MARK: - Notifications

    static let notifications: [AppNotification] = [
        AppNotification(
            id: 4, type: .tradeFilled, title: "Order filled",
            body: "Bought 5 NVDA at $164.80 for $824.00.",
            relatedId: 5, read: false, readAt: nil, createdAt: minutesAgo(60 * 48)
        ),
        AppNotification(
            id: 3, type: .lessonReward, title: "Lesson complete",
            body: "You earned $50.00 for finishing \"How ETFs Work\". It's been added to your buying power.",
            relatedId: 3, read: false, readAt: nil, createdAt: minutesAgo(60 * 24 * 7)
        ),
        AppNotification(
            id: 2, type: .tradeFilled, title: "Order filled",
            body: "Sold 1 TSLA at $246.10 for $246.10.",
            relatedId: 4, read: true, readAt: minutesAgo(60 * 24 * 6), createdAt: minutesAgo(60 * 24 * 6)
        ),
        AppNotification(
            id: 1, type: .lessonReward, title: "Lesson complete",
            body: "You earned $50.00 for finishing \"Bonds 101\". It's been added to your buying power.",
            relatedId: 2, read: true, readAt: minutesAgo(60 * 24 * 12), createdAt: minutesAgo(60 * 24 * 12)
        )
    ]

    // MARK: - Builders

    private static func decimal(_ string: String) -> Decimal {
        Decimal(string: string, locale: Locale(identifier: "en_US_POSIX")) ?? 0
    }

    private static func minutesAgo(_ minutes: Double) -> Date {
        Date.now.addingTimeInterval(-minutes * 60)
    }

    private static func makeIndex(id: String, name: String, value: Double, change: Double) -> MarketIndex {
        MarketIndex(
            id: id,
            name: name,
            value: value,
            changePercent: change,
            sparkline: PriceHistory.series(seed: id, range: .day, endValue: value, changePercent: change)
        )
    }

    private static func makeHolding(id: Int, ticker: String, quantity: Decimal, averageCost: String) -> Holding? {
        guard let asset = assets.first(where: { $0.ticker == ticker }) else {
            return nil
        }

        let cost = decimal(averageCost)
        let costBasis = (quantity * cost).rounded()
        let marketValue = (quantity * asset.currentPrice).rounded()
        let gain = marketValue - costBasis
        let gainPercent = costBasis > 0 ? (gain / costBasis * 100).rounded() : 0

        return Holding(
            id: id,
            assetId: asset.id,
            ticker: asset.ticker,
            name: asset.name,
            assetType: asset.assetType,
            quantity: quantity,
            averageCost: cost,
            currentPrice: asset.currentPrice,
            costBasis: costBasis,
            marketValue: marketValue,
            unrealizedGain: gain,
            unrealizedGainPercent: gainPercent
        )
    }

    private static func makePortfolio() -> Portfolio {
        let holdings = [
            makeHolding(id: 1, ticker: "NVDA", quantity: 15, averageCost: "131.20"),
            makeHolding(id: 2, ticker: "AAPL", quantity: 12, averageCost: "182.40"),
            makeHolding(id: 3, ticker: "VOO", quantity: 4, averageCost: "512.75"),
            makeHolding(id: 4, ticker: "MSFT", quantity: 3, averageCost: "398.10"),
            makeHolding(id: 5, ticker: "TSLA", quantity: 2, averageCost: "251.90")
        ].compactMap { $0 }

        let holdingsValue = holdings.reduce(Decimal(0)) { $0 + $1.marketValue }
        let costBasis = holdings.reduce(Decimal(0)) { $0 + $1.costBasis }
        let lessonRewards = lessons
            .filter { lessonProgress[$0.id]?.completed == true }
            .reduce(Decimal(0)) { $0 + $1.cashReward }
        let cash = startingBalance - costBasis + lessonRewards
        let gain = holdingsValue - costBasis

        return Portfolio(
            id: 1,
            name: "Main",
            startingBalance: startingBalance,
            cashAvailable: cash,
            holdingsValue: holdingsValue,
            currentPortfolioValue: cash + holdingsValue,
            investedCostBasis: costBasis,
            unrealizedGain: gain,
            unrealizedGainPercent: costBasis > 0 ? (gain / costBasis * 100).rounded() : 0,
            createdAt: Date.now.addingTimeInterval(-86_400 * 40),
            updatedAt: .now,
            holdings: holdings
        )
    }

    private static func makeTransaction(
        id: Int,
        ticker: String,
        type: Transaction.TransactionType,
        quantity: Decimal,
        price: String,
        daysAgo: Double
    ) -> Transaction {
        let assetID = assets.first(where: { $0.ticker == ticker })?.id ?? 0
        let price = decimal(price)

        return Transaction(
            id: id,
            assetId: assetID,
            ticker: ticker,
            transactionType: type,
            quantity: quantity,
            price: price,
            totalAmount: (quantity * price).rounded(),
            executedAt: Date.now.addingTimeInterval(-86_400 * daysAgo)
        )
    }

    private static func makeProgress(
        lessonID: Int,
        completed: Bool,
        checkpoint: Int,
        completedCheckpoints: Int,
        daysAgo: Double
    ) -> LessonProgress {
        let lastAccessed = Date.now.addingTimeInterval(-86_400 * daysAgo)

        return LessonProgress(
            lessonId: lessonID,
            completed: completed,
            currentCheckpoint: checkpoint,
            completedCheckpoints: completedCheckpoints,
            startedAt: lastAccessed.addingTimeInterval(-3_600),
            lastAccessedAt: lastAccessed,
            completedAt: completed ? lastAccessed : nil
        )
    }
}
