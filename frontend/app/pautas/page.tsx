"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

type Pauta = {
  id: string;
  name: string;
  yesCount: number;
  noCount: number;
  hasSession: boolean;
};

export default function PautasListPage() {
  const router = useRouter();
  const [pautas, setPautas] = useState<Pauta[]>([]);
  const [status, setStatus] = useState<"idle" | "loading" | "error">("idle");
  const [error, setError] = useState<string | null>(null);
  const [startingId, setStartingId] = useState<string | null>(null);

  const startSession = async (pautaId: string) => {
    setStartingId(pautaId);
    setError(null);

    try {
      const response = await fetch(`http://localhost:8080/api/v1/pautas/${pautaId}/sessions`, {
        method: "POST"
      });

      if (!response.ok) {
        throw new Error("Falha ao iniciar sessão");
      }

      const data = (await response.json()) as { sessionId: string };
      router.push(`/pautas/${pautaId}/session?sessionId=${data.sessionId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro inesperado");
    } finally {
      setStartingId(null);
    }
  };

  useEffect(() => {
    let isMounted = true;
    const loadPautas = async () => {
      setStatus("loading");
      setError(null);

      try {
        const response = await fetch("http://localhost:8080/api/v1/pautas", {
          method: "GET"
        });

        if (!response.ok) {
          throw new Error("Falha ao carregar pautas");
        }

        const data = (await response.json()) as Pauta[];
        if (isMounted) {
          setPautas(data);
          setStatus("idle");
        }
      } catch (err) {
        if (isMounted) {
          setStatus("error");
          setError(err instanceof Error ? err.message : "Erro inesperado");
        }
      }
    };

    loadPautas();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <main className="page">
      <section className="card card-wide">
        <div className="row">
          <h1 className="title">Pautas cadastradas</h1>
          <a href="/" className="button button-secondary">
            Voltar
          </a>
        </div>

        {status === "loading" && (
          <p className="muted spacing-top">Carregando pautas...</p>
        )}
        {status === "error" && error && (
          <div className="alert error spacing-top">{error}</div>
        )}

        {status !== "loading" && status !== "error" && (
          <div className="table-wrapper">
            {pautas.length === 0 ? (
              <div className="empty-state">Nenhuma pauta cadastrada.</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Pauta</th>
                    <th>Sim</th>
                    <th>Não</th>
                    <th>Sessão</th>
                  </tr>
                </thead>
                <tbody>
                  {pautas.map((pauta) => (
                    <tr key={pauta.id}>
                      <td>{pauta.name}</td>
                      <td>{pauta.yesCount}</td>
                      <td>{pauta.noCount}</td>
                      <td>
                        {pauta.hasSession ? (
                          <span className="pill">DONE</span>
                        ) : (
                          <button
                            type="button"
                            onClick={() => startSession(pauta.id)}
                            disabled={startingId === pauta.id}
                            className="button button-primary"
                          >
                            {startingId === pauta.id ? "Abrindo..." : "Iniciar sessão"}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </section>
    </main>
  );
}
