// Services/Network/JSONCoding.swift

import Foundation

/// Spring serializes `LocalDateTime` as "2026-10-26T14:03:12.123456" and
/// `LocalDate` as "2026-10-26": ISO-8601 with no time zone. Both are read
/// in the device's time zone, which matches the server while it runs on
/// localhost.
enum BackendDateFormat {
    static func date(from string: String) -> Date? {
        // Java prints anywhere from 0 to 9 fractional digits, and
        // DateFormatter can't express that, so drop them.
        let withoutFraction = String(
            string.split(separator: ".", maxSplits: 1)[0]
        )

        return dateTime.date(from: withoutFraction)
            ?? dateOnly.date(from: withoutFraction)
    }

    static func dateOnlyString(from date: Date) -> String {
        dateOnly.string(from: date)
    }

    private static let dateTime = makeFormatter("yyyy-MM-dd'T'HH:mm:ss")
    private static let dateOnly = makeFormatter("yyyy-MM-dd")

    private static func makeFormatter(_ format: String) -> DateFormatter {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.timeZone = .current
        formatter.dateFormat = format
        return formatter
    }
}

extension JSONDecoder {
    static let betwise: JSONDecoder = {
        let decoder = JSONDecoder()

        decoder.dateDecodingStrategy = .custom { decoder in
            let container = try decoder.singleValueContainer()
            let string = try container.decode(String.self)

            guard let date = BackendDateFormat.date(from: string) else {
                throw DecodingError.dataCorruptedError(
                    in: container,
                    debugDescription: "Unrecognized date: \(string)"
                )
            }

            return date
        }

        return decoder
    }()
}

extension JSONEncoder {
    static let betwise: JSONEncoder = {
        let encoder = JSONEncoder()

        // The only dates the app sends are `LocalDate` fields, such as
        // dateOfBirth, so encode the calendar day only.
        encoder.dateEncodingStrategy = .custom { date, encoder in
            var container = encoder.singleValueContainer()
            try container.encode(BackendDateFormat.dateOnlyString(from: date))
        }

        return encoder
    }()
}
