import SwiftUI
import Charts

struct PortfolioView: View {
    let model: PortfolioModel
    let market: MarketModel
    let session: SessionModel
    let notifications: NotificationsModel
    let potentialLessonRewards: Decimal
    let onOpenLearn: () -> Void

    @State private var isShowingProfile = false
    @State private var isShowingNotifications = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 28) {
                if model.isShowingSampleData {
                    SampleDataNotice {
                        session.signOut()
                    }
                }

                if !model.hasLoaded {
                    ProgressView()
                        .frame(maxWidth: .infinity, minHeight: 300)
                } else if !model.hasPortfolio {
                    CreatePortfolioCard(model: model)
                } else {
                    PortfolioHero(model: model)

                    BalanceBreakdownCard(
                        cash: model.cashAvailable,
                        invested: model.holdingsValue,
                        gain: model.unrealizedGain,
                        gainPercent: model.unrealizedGainPercent,
                        allocation: model.allocation
                    )

                    HoldingsSection(holdings: model.holdings, market: market)
                }

                if potentialLessonRewards > 0 {
                    LearnPromoCard(potentialReward: potentialLessonRewards, action: onOpenLearn)
                }

                if model.hasPortfolio {
                    ActivitySection(transactions: model.transactions)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .scrollIndicators(.hidden)
        .screenBackground()
        .navigationTitle("Portfolio")
        .toolbar {
            ToolbarItem(placement: .navigation) {
                Button {
                    isShowingProfile = true
                } label: {
                    Label("Profile", systemImage: "person.crop.circle")
                }
            }

            ToolbarItem(placement: .primaryAction) {
                Button {
                    isShowingNotifications = true
                } label: {
                    Label("Notifications", systemImage: "bell")
                }
                .badge(notifications.unreadCount)
                .accessibilityValue(
                    notifications.unreadCount > 0
                        ? Text("\(notifications.unreadCount) unread")
                        : Text("")
                )
            }
        }
        .sheet(isPresented: $isShowingProfile) {
            ProfileView(session: session)
        }
        .sheet(isPresented: $isShowingNotifications) {
            NotificationsView(model: notifications)
        }
        .assetDestinations(market: market, portfolio: model)
        .refreshable {
            await model.refresh()
            await notifications.refreshUnreadCount()
        }
        .task {
            await model.refresh()
            await notifications.refreshUnreadCount()
        }
        // A new trade means the server just sent an "Order filled"
        // notification, so update the bell from any tab.
        .onChange(of: model.transactions.first?.id) {
            Task {
                await notifications.refreshUnreadCount()
            }
        }
    }
}

// MARK: - Sections

private struct SampleDataNotice: View {
    let onSignIn: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Label("You're exploring with sample data.", systemImage: "info.circle")
                .font(.footnote)
                .foregroundStyle(AppColors.textSecondary)

            Spacer(minLength: 8)

            Button("Sign in") {
                onSignIn()
            }
            .font(.footnote.weight(.semibold))
            .foregroundStyle(AppColors.accentBright)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(AppColors.accent.opacity(0.1), in: .rect(cornerRadius: 14, style: .continuous))
    }
}

/// Shown to a new account until they've created a portfolio.
private struct CreatePortfolioCard: View {
    let model: PortfolioModel

    @State private var name = String(localized: "My Portfolio")
    @State private var startingBalance: StartingBalance = .tenThousand
    @State private var isWorking = false
    @State private var errorMessage: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            VStack(alignment: .leading, spacing: 8) {
                Image(systemName: "chart.pie.fill")
                    .font(.title)
                    .foregroundStyle(AppColors.accentBright)
                    .accessibilityHidden(true)

                Text("Create your portfolio")
                    .font(.title2.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("Pick how much practice money to start with. You can earn more by finishing lessons.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }

            FormField(title: "Name", text: $name)

            VStack(alignment: .leading, spacing: 8) {
                Text("Starting balance")
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(AppColors.textSecondary)

                FilterChips(options: StartingBalance.options, selection: $startingBalance)
            }

            FormError(message: errorMessage)

            PrimaryButton(title: "Create portfolio", isWorking: isWorking) {
                Task {
                    await create()
                }
            }
            .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty || isWorking)
        }
        .card(padding: 20)
    }

    private func create() async {
        isWorking = true
        errorMessage = nil

        defer { isWorking = false }

        do {
            try await model.createPortfolio(
                name: name.trimmingCharacters(in: .whitespaces),
                startingBalance: startingBalance.amount
            )
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

private enum StartingBalance: Int, CaseIterable, Hashable {
    case oneThousand = 1_000
    case fiveThousand = 5_000
    case tenThousand = 10_000
    case fiftyThousand = 50_000

    var amount: Decimal {
        Decimal(rawValue)
    }

    static let options: [FilterOption<StartingBalance>] = allCases.map { balance in
        FilterOption(
            value: balance,
            title: balance.amount.formatted(.currency(code: "USD").precision(.fractionLength(0)))
        )
    }
}

private struct PortfolioHero: View {
    @Bindable var model: PortfolioModel

    @State private var selectedIndex: Int?

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            ChartValueHeader(
                title: "Total balance",
                currentValue: model.totalValue,
                points: model.history,
                selectedIndex: selectedIndex,
                range: model.range
            )

            PriceChart(
                points: model.history,
                tint: AppColors.accent,
                selectedIndex: $selectedIndex
            )
            .frame(height: 190)

            RangePicker(selection: $model.range)
        }
        .padding(.top, 4)
    }
}

private struct BalanceBreakdownCard: View {
    let cash: Decimal
    let invested: Decimal
    let gain: Decimal
    let gainPercent: Double
    let allocation: [AllocationSlice]

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack(alignment: .top) {
                BalanceStat(title: "Buying power", value: cash)
                Spacer()
                BalanceStat(title: "Invested", value: invested)
                Spacer()

                VStack(alignment: .trailing, spacing: 4) {
                    Text("Total return")
                        .font(.caption)
                        .foregroundStyle(AppColors.textSecondary)

                    Text(gain, format: .currency(code: "USD").sign(strategy: .always()))
                        .font(.subheadline.weight(.semibold))
                        .monospacedDigit()
                        .foregroundStyle(AppColors.movement(gainPercent))

                    ChangeBadge(percent: gainPercent)
                }
                .accessibilityElement(children: .combine)
            }

            if !allocation.isEmpty {
                AllocationBar(slices: allocation)
            }
        }
        .card()
    }
}

private struct BalanceStat: View {
    let title: LocalizedStringKey
    let value: Decimal

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundStyle(AppColors.textSecondary)

            Text(value, format: .currency(code: "USD"))
                .font(.subheadline.weight(.semibold))
                .monospacedDigit()
                .foregroundStyle(AppColors.textPrimary)
        }
        .accessibilityElement(children: .combine)
    }
}

private struct AllocationBar: View {
    let slices: [AllocationSlice]

    private let columns = [GridItem(.flexible(), alignment: .leading), GridItem(.flexible(), alignment: .leading)]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Allocation")
                .font(.caption.weight(.semibold))
                .foregroundStyle(AppColors.textSecondary)

            Chart(slices) { slice in
                BarMark(
                    x: .value("Share", slice.fraction),
                    y: .value("Portfolio", "Allocation")
                )
                .foregroundStyle(color(for: slice))
            }
            .chartXScale(domain: 0...1)
            .chartXAxis(.hidden)
            .chartYAxis(.hidden)
            .chartLegend(.hidden)
            .frame(height: 10)
            .clipShape(Capsule())
            .accessibilityHidden(true)

            LazyVGrid(columns: columns, spacing: 10) {
                ForEach(slices) { slice in
                    HStack(spacing: 8) {
                        Circle()
                            .fill(color(for: slice))
                            .frame(width: 8, height: 8)

                        Text(slice.label)
                            .font(.footnote.weight(.medium))
                            .foregroundStyle(AppColors.textPrimary)

                        Text(slice.fraction, format: .percent.precision(.fractionLength(0)))
                            .font(.footnote)
                            .monospacedDigit()
                            .foregroundStyle(AppColors.textSecondary)
                    }
                    .accessibilityElement(children: .combine)
                }
            }
        }
    }

    private func color(for slice: AllocationSlice) -> Color {
        slice.isCash ? AppColors.cash : AppColors.paletteColor(for: slice.id)
    }
}

private struct HoldingsSection: View {
    let holdings: [Holding]
    let market: MarketModel

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            SectionHeader(title: "Holdings", detail: holdings.count.formatted())

            if holdings.isEmpty {
                Text("You don't own anything yet. Explore the market to make your first trade.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
                    .card()
            } else {
                VStack(spacing: 0) {
                    ForEach(holdings) { holding in
                        HoldingLink(holding: holding, destination: market.rowsByAssetID[holding.assetId])

                        if holding.id != holdings.last?.id {
                            Divider().overlay(AppColors.stroke)
                        }
                    }
                }
                .card(padding: 16)
            }
        }
    }
}

private struct HoldingLink: View {
    let holding: Holding
    let destination: MarketRowItem?

    var body: some View {
        if let destination {
            NavigationLink(value: destination) {
                HoldingRow(holding: holding)
            }
            .buttonStyle(.plain)
        } else {
            HoldingRow(holding: holding)
        }
    }
}

private struct HoldingRow: View {
    let holding: Holding

    var body: some View {
        HStack(spacing: 14) {
            TickerLogo(ticker: holding.ticker)

            VStack(alignment: .leading, spacing: 3) {
                Text(holding.ticker)
                    .font(.headline)
                    .foregroundStyle(AppColors.textPrimary)

                Text("\(holding.quantity.formatted()) shares")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
            }

            Spacer(minLength: 8)

            VStack(alignment: .trailing, spacing: 4) {
                Text(holding.marketValue, format: .currency(code: "USD"))
                    .font(.headline)
                    .monospacedDigit()
                    .foregroundStyle(AppColors.textPrimary)

                ChangeBadge(percent: holding.unrealizedGainPercent.doubleValue)
            }
        }
        .padding(.vertical, 12)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

private struct LearnPromoCard: View {
    let potentialReward: Decimal
    let action: () -> Void

    var body: some View {
        HStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 8) {
                Text("Learn & earn")
                    .font(.headline)
                    .foregroundStyle(.white)

                Text("Finish lessons to add up to \(potentialReward, format: .currency(code: "USD").precision(.fractionLength(0))) to your buying power.")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.8))
                    .fixedSize(horizontal: false, vertical: true)

                Button("Start learning") {
                    action()
                }
                    .buttonStyle(.glassProminent)
                    .tint(.white.opacity(0.22))
                    .padding(.top, 4)
            }

            Spacer(minLength: 0)

            Image(systemName: "graduationcap.fill")
                .font(.system(size: 44))
                .foregroundStyle(.white.opacity(0.9))
                .accessibilityHidden(true)
        }
        .padding(20)
        .background(
            LinearGradient(
                colors: [AppColors.accent, AppColors.accentDeep],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: .rect(cornerRadius: 24, style: .continuous)
        )
    }
}

private struct ActivitySection: View {
    let transactions: [Transaction]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            SectionHeader(title: "Recent activity")

            if transactions.isEmpty {
                Text("Your trades will appear here.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
                    .card()
            } else {
                VStack(spacing: 0) {
                    ForEach(transactions.prefix(5)) { transaction in
                        TransactionRow(transaction: transaction)

                        if transaction.id != transactions.prefix(5).last?.id {
                            Divider().overlay(AppColors.stroke)
                        }
                    }
                }
                .card(padding: 16)
            }
        }
    }
}

private struct TransactionRow: View {
    let transaction: Transaction

    var body: some View {
        let isBuy = transaction.transactionType == .buy

        HStack(spacing: 14) {
            Image(systemName: isBuy ? "plus" : "minus")
                .font(.subheadline.weight(.bold))
                .foregroundStyle(isBuy ? AppColors.accentBright : AppColors.textSecondary)
                .frame(width: 40, height: 40)
                .background(
                    (isBuy ? AppColors.accent.opacity(0.16) : AppColors.surfaceRaised),
                    in: Circle()
                )
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 3) {
                Text(isBuy ? "Bought \(transaction.ticker)" : "Sold \(transaction.ticker)")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(AppColors.textPrimary)

                Text(transaction.executedAt, format: .dateTime.month(.abbreviated).day())
                    .font(.caption)
                    .foregroundStyle(AppColors.textSecondary)
            }

            Spacer(minLength: 8)

            VStack(alignment: .trailing, spacing: 3) {
                Text(transaction.totalAmount, format: .currency(code: "USD"))
                    .font(.subheadline.weight(.semibold))
                    .monospacedDigit()
                    .foregroundStyle(AppColors.textPrimary)

                Text("\(transaction.quantity.formatted()) × \(transaction.price, format: .currency(code: "USD"))")
                    .font(.caption)
                    .monospacedDigit()
                    .foregroundStyle(AppColors.textSecondary)
            }
        }
        .padding(.vertical, 12)
        .accessibilityElement(children: .combine)
    }
}

#Preview {
    NavigationStack {
        PortfolioView(
            model: PortfolioModel(usesSampleData: true),
            market: MarketModel(usesSampleData: true),
            session: SessionModel(),
            notifications: NotificationsModel(usesSampleData: true),
            potentialLessonRewards: 575,
            onOpenLearn: {}
        )
    }
    .preferredColorScheme(.dark)
}
