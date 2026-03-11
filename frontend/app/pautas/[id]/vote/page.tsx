"use client";

import { useEffect, useState } from "react";
import { useParams, useSearchParams } from "next/navigation";

type SessionResponse = {
  sessionId: string;
  pautaId: string;
  pautaName: string;
  openedAt: string;
  closesAt: string;
  closed: boolean;
  yesCount: number;
  noCount: number;
};

type VoteChoice = "YES" | "NO";

export default function PautaVotePage() {
  const params = useParams<{ id: string }>();
  const searchParams = useSearchParams();
  const sessionId = searchParams.get("sessionId");
  const pautaId = params.id;

  const [session, setSession] = useState<SessionResponse | null>(null);
  const [choice, setChoice] = useState<VoteChoice | null>(null);
  const [status, setStatus] = useState<"idle" | "loading" | "submitting" | "error" | "success">("idle");
  const [error, setError] = useState<string | null>(null);
  const [userId, setUserId] = useState<string>("");

  useEffect(() => {
    if (!sessionId) {
      setError("Sessão inválida");
      return;
    }

    let isMounted = true;

    const loadSession = async () => {
      setStatus("loading");
      setError(null);

      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/pautas/${pautaId}/sessions/${sessionId}`,
          { method: "GET" }
        );

        if (!response.ok) {
          throw new Error("Falha ao carregar sessão");
        }

        const data = (await response.json()) as SessionResponse;
        if (isMounted) {
          setSession(data);
          setStatus("idle");
        }
      } catch (err) {
        if (isMounted) {
          setStatus("error");
          setError(err instanceof Error ? err.message : "Erro inesperado");
        }
      }
    };

    loadSession();

    return () => {
      isMounted = false;
    };
  }, [pautaId, sessionId]);

  const submitVote = async () => {
    if (!sessionId || !choice || userId.trim().length === 0) {
      return;
    }

    setStatus("submitting");
    setError(null);

    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/pautas/${pautaId}/sessions/${sessionId}/votes`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ userId, choice })
        }
      );

      if (!response.ok) {
        throw new Error("Falha ao enviar voto");
      }

      const data = (await response.json()) as SessionResponse;
      setSession(data);
      setStatus("success");
    } catch (err) {
      setStatus("error");
      setError(err instanceof Error ? err.message : "Erro inesperado");
    }
  };

  return (
    <main className="page">
      <section className="card">
        <h1 className="title">Votar na pauta</h1>

        {status === "error" && error && (
          <div className="alert error spacing-top">{error}</div>
        )}

        {status === "loading" && !session && (
          <p className="muted spacing-top">Carregando sessão...</p>
        )}

        {session && (
          <div className="stack">
            <div>
              <p className="muted">Pauta</p>
              <p className="title-sm">{session.pautaName}</p>
            </div>

            {session.closed ? (
              <div className="alert success">
                Sessão finalizada. Resultado: {session.yesCount} Sim / {session.noCount} Não.
              </div>
            ) : (
              <>
                <div>
                  <label className="label" htmlFor="user-id">
                    CPF do usuário
                  </label>
                  <input
                    id="user-id"
                    name="user-id"
                    type="text"
                    value={userId}
                    onChange={(event) => setUserId(event.target.value)}
                    className="input"
                    placeholder="Ex: 123.456.789-00"
                    required
                  />
                </div>

                <div className="actions">
                  <button
                    type="button"
                    onClick={() => setChoice("YES")}
                    className={`button ${choice === "YES" ? "selected-yes" : "button-secondary"}`}
                  >
                    Sim
                  </button>
                  <button
                    type="button"
                    onClick={() => setChoice("NO")}
                    className={`button ${choice === "NO" ? "selected-no" : "button-secondary"}`}
                  >
                    Não
                  </button>
                </div>

                <button
                  type="button"
                  onClick={submitVote}
                  disabled={status === "submitting" || !choice || userId.trim().length === 0}
                  className="button button-primary"
                >
                  {status === "submitting" ? "Enviando..." : "Enviar voto"}
                </button>

                {status === "success" && (
                  <div className="alert success">Voto registrado.</div>
                )}
              </>
            )}
          </div>
        )}
      </section>
    </main>
  );
}
