// Services/News/NewsService.swift

import Foundation

final class NewsService {
    static let shared = NewsService()

    private let api = APIClient.shared

    private init() {}

    func getNews() async throws -> [NewsArticle] {
        try await api.request(
            .get,
            "/api/news"
        )
    }
}
