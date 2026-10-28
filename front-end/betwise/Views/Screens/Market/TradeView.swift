import SwiftUI

enum TradeSide: String, CaseIterable, Identifiable {
    case buy
    case sell

    var id: Self { self }

    var title: String {
        switch self {
        case .buy: String(localized: "Buy")
        case .sell: String(localized: "Sell")
        }
    }
}

/// The order ticket, presented as a sheet from an asset's page.
struct TradeView: View {
    let assetID: Int
    let ticker: String
    let price: Decimal
    let portfolio: PortfolioModel

    @Environment(\.dismiss) private var dismiss

    @State private var side: TradeSide
    @State private var quantityText = ""
    @State private var isSubmitting = false
    @State private var errorMessage: String?
    @State private var filledOrder: Transaction?
    @FocusState private var isQuantityFocused: Bool

    init(
        assetID: Int,
        ticker: String,
        price: Decimal,
        initialSide: TradeSide,
        portfolio: PortfolioModel
    ) {
        self.assetID = assetID
        self.ticker = ticker
        self.price = price
        self.portfolio = portfolio
        self.side = initialSide
    }

    var body: some View {
        NavigationStack {
            Group {
                if let filledOrder {
                    OrderFilledView(transaction: filledOrder) {
                        dismiss()
                    }
                } else {
                    orderForm
                }
            }
            .screenBackground()
            .navigationTitle("\(side.title) \(ticker)")
            #if os(iOS)
            .navigationBarTitleDisplayMode(.inline)
            #endif
            .toolbar {
                if filledOrder == nil {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel", role: .cancel) {
                            dismiss()
                        }
                    }
                }
            }
        }
        .presentationBackground(AppColors.background)
    }

    // A form whose pieces all read the same local state; splitting it into
    // separate views would only add bindings.
    private var orderForm: some View {
        let quantity = parsedQuantity
        let total = ((quantity ?? 0) * price).rounded()
        let owned = portfolio.holding(forAssetID: assetID)?.quantity ?? 0
        let validationMessage = validationMessage(quantity: quantity, total: total, owned: owned)

        return VStack(spacing: 24) {
            Picker("Order type", selection: $side) {
                ForEach(TradeSide.allCases) { side in
                    Text(side.title).tag(side)
                }
            }
            .pickerStyle(.segmented)

            Spacer(minLength: 0)

            VStack(spacing: 8) {
                TextField("0", text: $quantityText)
                    .font(.system(size: 64, weight: .bold, design: .rounded))
                    .monospacedDigit()
                    .multilineTextAlignment(.center)
                    .foregroundStyle(AppColors.textPrimary)
                    .focused($isQuantityFocused)
                    #if os(iOS)
                    .keyboardType(.decimalPad)
                    #endif
                    .accessibilityLabel("Number of shares")

                Text("shares of \(ticker)")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)

                if let validationMessage {
                    Text(validationMessage)
                        .font(.footnote.weight(.medium))
                        .foregroundStyle(AppColors.loss)
                        .padding(.top, 4)
                }
            }

            Spacer(minLength: 0)

            VStack(spacing: 14) {
                SummaryRow(title: "Market price") {
                    Text(price, format: .currency(code: "USD"))
                }

                Divider().overlay(AppColors.stroke)

                switch side {
                case .buy:
                    SummaryRow(title: "Buying power") {
                        Text(portfolio.cashAvailable, format: .currency(code: "USD"))
                    }
                case .sell:
                    SummaryRow(title: "Shares owned") {
                        Text(owned.formatted())
                    }
                }

                Divider().overlay(AppColors.stroke)

                SummaryRow(title: side == .buy ? "Estimated cost" : "Estimated credit") {
                    Text(total, format: .currency(code: "USD"))
                        .fontWeight(.bold)
                }
            }
            .card()

            if let errorMessage {
                Text(errorMessage)
                    .font(.footnote)
                    .foregroundStyle(AppColors.loss)
                    .multilineTextAlignment(.center)
            }

            Button {
                Task {
                    await submit(quantity: quantity)
                }
            } label: {
                Group {
                    if isSubmitting {
                        ProgressView()
                    } else {
                        Text("Place \(side.title.lowercased()) order")
                    }
                }
                .font(.headline)
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.glassProminent)
            .tint(AppColors.accent)
            .controlSize(.large)
            .buttonBorderShape(.capsule)
            .disabled(quantity == nil || validationMessage != nil || isSubmitting)
        }
        .padding(20)
        .onAppear {
            isQuantityFocused = true
        }
    }

    private var parsedQuantity: Decimal? {
        let trimmed = quantityText.trimmingCharacters(in: .whitespaces)

        guard let value = Decimal(string: trimmed, locale: .current), value > 0 else {
            return nil
        }

        return value
    }

    private func validationMessage(quantity: Decimal?, total: Decimal, owned: Decimal) -> String? {
        guard let quantity else { return nil }

        switch side {
        case .buy where total > portfolio.cashAvailable:
            return String(localized: "Not enough buying power")
        case .sell where quantity > owned:
            return String(localized: "You only own \(owned.formatted()) shares")
        default:
            return nil
        }
    }

    private func submit(quantity: Decimal?) async {
        guard let quantity else { return }

        guard !portfolio.isShowingSampleData else {
            errorMessage = String(localized: "Sign in to place trades with your Betwise account.")
            return
        }

        guard let portfolioID = portfolio.portfolioID else {
            errorMessage = String(localized: "Create a portfolio on the Portfolio tab before trading.")
            return
        }

        isSubmitting = true
        errorMessage = nil

        defer { isSubmitting = false }

        do {
            let transaction = switch side {
            case .buy:
                try await PortfolioService.shared.buy(portfolioId: portfolioID, assetId: assetID, quantity: quantity)
            case .sell:
                try await PortfolioService.shared.sell(portfolioId: portfolioID, assetId: assetID, quantity: quantity)
            }

            withAnimation(.snappy) {
                filledOrder = transaction
            }

            await portfolio.refresh()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

private struct SummaryRow<Value: View>: View {
    let title: LocalizedStringKey
    @ViewBuilder let value: Value

    var body: some View {
        HStack {
            Text(title)
                .foregroundStyle(AppColors.textSecondary)

            Spacer()

            value
                .monospacedDigit()
                .foregroundStyle(AppColors.textPrimary)
        }
        .font(.subheadline)
        .accessibilityElement(children: .combine)
    }
}

private struct OrderFilledView: View {
    let transaction: Transaction
    let onDone: () -> Void

    var body: some View {
        let isBuy = transaction.transactionType == .buy

        VStack(spacing: 20) {
            Spacer()

            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 72))
                .foregroundStyle(AppColors.accent)
                .symbolEffect(.bounce, value: transaction.id)
                .accessibilityHidden(true)

            VStack(spacing: 8) {
                Text("Order filled")
                    .font(.title.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text(
                    isBuy
                        ? "Bought \(transaction.quantity.formatted()) \(transaction.ticker) at \(transaction.price, format: .currency(code: "USD"))"
                        : "Sold \(transaction.quantity.formatted()) \(transaction.ticker) at \(transaction.price, format: .currency(code: "USD"))"
                )
                .font(.subheadline)
                .foregroundStyle(AppColors.textSecondary)
                .multilineTextAlignment(.center)
            }

            Spacer()

            Button {
                onDone()
            } label: {
                Text("Done")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.glassProminent)
            .tint(AppColors.accent)
            .controlSize(.large)
            .buttonBorderShape(.capsule)
        }
        .padding(20)
    }
}

#Preview {
    TradeView(
        assetID: 1,
        ticker: "AAPL",
        price: 200,
        initialSide: .buy,
        portfolio: PortfolioModel(usesSampleData: true)
    )
    .preferredColorScheme(.dark)
}
