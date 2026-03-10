import "./globals.css";
import type { ReactNode } from "react";

export const metadata = {
  title: "Votacao Assembleia",
  description: "Hello World from backend"
};

type RootLayoutProps = {
  children: ReactNode;
};

export default function RootLayout({ children }: RootLayoutProps) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
