// Models/Quiz/QuizQuestion.swift

import Foundation

struct QuizQuestion: Codable, Identifiable, Hashable {
    let id: Int
    let question: String

    /// The raw choices string exactly as the server stores it.
    let choices: String

    let questionOrder: Int

    /// The choices split into separate answers. The backend keeps them in a
    /// single text column, so this accepts a JSON array
    /// (`["A","B"]`) and falls back to a comma-separated list.
    var choiceList: [String] {
        if let data = choices.data(using: .utf8),
           let list = try? JSONDecoder().decode([String].self, from: data) {
            return list
        }

        return choices
            .split(separator: ",")
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }
    }
}
