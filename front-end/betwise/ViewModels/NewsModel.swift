// ViewModels/NewsModel.swift

import Foundation
import Observation

@Observable
final class NewsModel {
    private(set) var articles: [NewsArticle] = []
    private(set) var hasLoaded: Bool
    private(set) var errorMessage: String?

    /// Guests read sample stories; signed-in users get the backend's.
    let usesSampleData: Bool

    init(usesSampleData: Bool) {
        self.usesSampleData = usesSampleData
        hasLoaded = usesSampleData

        if usesSampleData {
            articles = SampleData.news
        }
    }

    func refresh() async {
        guard !usesSampleData else { return }

        defer { hasLoaded = true }

        do {
            articles = try await NewsService.shared.getNews()
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
