import SwiftUI

/// A labeled text field on a dark rounded surface, used by the auth and
/// profile forms.
struct FormField: View {
    enum Kind {
        case text
        case email
        case name
        case newPassword
        case password
    }

    let title: LocalizedStringKey
    @Binding var text: String
    var kind: Kind = .text
    var prompt: LocalizedStringKey?

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(AppColors.textSecondary)

            field
                .font(.body)
                .foregroundStyle(AppColors.textPrimary)
                .padding(.horizontal, 16)
                .frame(minHeight: 52)
                .background(AppColors.surface, in: .rect(cornerRadius: 16, style: .continuous))
                .overlay {
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .strokeBorder(AppColors.stroke)
                }
        }
    }

    @ViewBuilder
    private var field: some View {
        let placeholder = Text(prompt ?? "").foregroundStyle(AppColors.textTertiary)

        switch kind {
        case .password, .newPassword:
            SecureField(text: $text, prompt: placeholder) {
                Text(title)
            }
            .textContentType(kind == .newPassword ? .newPassword : .password)
            .autocorrectionDisabled()

        case .email:
            TextField(text: $text, prompt: placeholder) {
                Text(title)
            }
            .textContentType(.emailAddress)
            .autocorrectionDisabled()
            #if os(iOS)
            .keyboardType(.emailAddress)
            .textInputAutocapitalization(.never)
            #endif

        case .name:
            TextField(text: $text, prompt: placeholder) {
                Text(title)
            }
            .textContentType(.name)

        case .text:
            TextField(text: $text, prompt: placeholder) {
                Text(title)
            }
        }
    }
}

/// A full-width primary action with a spinner while work is in flight.
struct PrimaryButton: View {
    let title: LocalizedStringKey
    var isWorking = false
    let action: () -> Void

    var body: some View {
        Button {
            action()
        } label: {
            Group {
                if isWorking {
                    ProgressView()
                } else {
                    Text(title)
                }
            }
            .font(.headline)
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.glassProminent)
        .tint(AppColors.accent)
        .controlSize(.large)
        .buttonBorderShape(.capsule)
    }
}

/// An inline error message under a form.
struct FormError: View {
    let message: String?

    var body: some View {
        if let message {
            Label(message, systemImage: "exclamationmark.circle.fill")
                .font(.footnote.weight(.medium))
                .foregroundStyle(AppColors.loss)
                .frame(maxWidth: .infinity, alignment: .leading)
                .transition(.opacity)
        }
    }
}
