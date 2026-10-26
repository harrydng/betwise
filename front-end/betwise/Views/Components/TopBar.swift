//
//  TopBar.swift
//  betwise
//
//  Created by Harry Duong on 9/16/26.
//

import SwiftUI

struct TopBar: View {

    var body: some View {

        HStack {

            Button {
                print("Notifications")
            } label: {

                Image(systemName: "bell.fill")
                    .foregroundStyle(AppColors.textPrimary)
                    .frame(width: 44, height: 44)
                    .background(AppColors.surface)
                    .clipShape(Circle())
            }

            Spacer()

//            Text("BETWISE")
//                .font(.system(size: 20, weight: .bold))
//                .foregroundStyle(AppColors.textPrimary)
//
//            Spacer()

            Button {
                print("Profile")
            } label: {

                Image(systemName: "person.fill")
                    .foregroundStyle(AppColors.textPrimary)
                    .frame(width: 44, height: 44)
                    .background(AppColors.surface)
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 20)
    }
}

#Preview {
    ContentView()
}
