--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: aposta_ganhadora; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.aposta_ganhadora (
    id integer NOT NULL,
    concurso_id integer NOT NULL,
    faixa_id integer NOT NULL,
    localidade character varying(100),
    uf character(2),
    nome_casa_loterica character varying(255)
);


ALTER TABLE public.aposta_ganhadora OWNER TO postgres;

--
-- Name: aposta_ganhadora_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.aposta_ganhadora_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.aposta_ganhadora_id_seq OWNER TO postgres;

--
-- Name: aposta_ganhadora_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.aposta_ganhadora_id_seq OWNED BY public.aposta_ganhadora.id;


--
-- Name: concurso; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.concurso (
    id integer NOT NULL,
    loteria_id integer NOT NULL,
    numero_concurso integer NOT NULL,
    data_concurso date NOT NULL,
    arrecadacao_total numeric(15,2),
    acumulado boolean DEFAULT false,
    valor_acumulado numeric(15,2),
    estimativa_premio numeric(15,2),
    acumulado_especial numeric(15,2),
    observacao text,
    time_coracao character varying(100)
);


ALTER TABLE public.concurso OWNER TO postgres;

--
-- Name: concurso_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.concurso_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concurso_id_seq OWNER TO postgres;

--
-- Name: concurso_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.concurso_id_seq OWNED BY public.concurso.id;


--
-- Name: concurso_numero_sorteado; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.concurso_numero_sorteado (
    id integer NOT NULL,
    concurso_id integer NOT NULL,
    numero integer NOT NULL,
    ordem integer NOT NULL
);


ALTER TABLE public.concurso_numero_sorteado OWNER TO postgres;

--
-- Name: concurso_numero_sorteado_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.concurso_numero_sorteado_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concurso_numero_sorteado_id_seq OWNER TO postgres;

--
-- Name: concurso_numero_sorteado_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.concurso_numero_sorteado_id_seq OWNED BY public.concurso_numero_sorteado.id;


--
-- Name: faixa_premiacao; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.faixa_premiacao (
    id integer NOT NULL,
    loteria_id integer NOT NULL,
    nome character varying(100) NOT NULL,
    acertos integer,
    ordem integer
);


ALTER TABLE public.faixa_premiacao OWNER TO postgres;

--
-- Name: faixa_premiacao_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.faixa_premiacao_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.faixa_premiacao_id_seq OWNER TO postgres;

--
-- Name: faixa_premiacao_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.faixa_premiacao_id_seq OWNED BY public.faixa_premiacao.id;


--
-- Name: ganhador; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ganhador (
    id integer NOT NULL,
    concurso_id integer NOT NULL,
    faixa_id integer NOT NULL,
    quantidade_ganhadores integer,
    premio_total numeric(15,2)
);


ALTER TABLE public.ganhador OWNER TO postgres;

--
-- Name: ganhador_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ganhador_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ganhador_id_seq OWNER TO postgres;

--
-- Name: ganhador_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ganhador_id_seq OWNED BY public.ganhador.id;


--
-- Name: jogo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.jogo (
    id integer NOT NULL,
    loteria_id integer NOT NULL,
    concurso_id integer,
    numero_concurso_previsto integer,
    numeros character varying(100) NOT NULL,
    data_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    acertos integer,
    observacao text
);


ALTER TABLE public.jogo OWNER TO postgres;

--
-- Name: jogo_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.jogo_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.jogo_id_seq OWNER TO postgres;

--
-- Name: jogo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.jogo_id_seq OWNED BY public.jogo.id;


--
-- Name: loteria; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.loteria (
    id integer NOT NULL,
    nome character varying(50) NOT NULL,
    descricao text,
    quantidade_numeros_aposta_min integer NOT NULL,
    quantidade_numeros_aposta_max integer NOT NULL,
    quantidade_numeros_sorteados integer NOT NULL,
    intervalo_numeros_min integer NOT NULL,
    intervalo_numeros_max integer NOT NULL,
    preco_aposta_min numeric(10,2) NOT NULL,
    preco_aposta_max numeric(10,2),
    periodicidade character varying(50)
);


ALTER TABLE public.loteria OWNER TO postgres;

--
-- Name: loteria_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.loteria_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.loteria_id_seq OWNER TO postgres;

--
-- Name: loteria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.loteria_id_seq OWNED BY public.loteria.id;


--
-- Name: probabilidade; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.probabilidade (
    id integer NOT NULL,
    loteria_id integer NOT NULL,
    faixa_id integer NOT NULL,
    numeros_apostados integer NOT NULL,
    probabilidade numeric(25,18)
);


ALTER TABLE public.probabilidade OWNER TO postgres;

--
-- Name: probabilidade_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.probabilidade_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.probabilidade_id_seq OWNER TO postgres;

--
-- Name: probabilidade_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.probabilidade_id_seq OWNED BY public.probabilidade.id;


--
-- Name: aposta_ganhadora id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aposta_ganhadora ALTER COLUMN id SET DEFAULT nextval('public.aposta_ganhadora_id_seq'::regclass);


--
-- Name: concurso id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.concurso ALTER COLUMN id SET DEFAULT nextval('public.concurso_id_seq'::regclass);


--
-- Name: concurso_numero_sorteado id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.concurso_numero_sorteado ALTER COLUMN id SET DEFAULT nextval('public.concurso_numero_sorteado_id_seq'::regclass);


--
-- Name: faixa_premiacao id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faixa_premiacao ALTER COLUMN id SET DEFAULT nextval('public.faixa_premiacao_id_seq'::regclass);


--
-- Name: ganhador id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ganhador ALTER COLUMN id SET DEFAULT nextval('public.ganhador_id_seq'::regclass);


--
-- Name: jogo id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.jogo ALTER COLUMN id SET DEFAULT nextval('public.jogo_id_seq'::regclass);


--
-- Name: loteria id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.loteria ALTER COLUMN id SET DEFAULT nextval('public.loteria_id_seq'::regclass);


--
-- Name: probabilidade id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.probabilidade ALTER COLUMN id SET DEFAULT nextval('public.probabilidade_id_seq'::regclass);


--
-- Name: aposta_ganhadora aposta_ganhadora_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aposta_ganhadora
    ADD CONSTRAINT aposta_ganhadora_pkey PRIMARY KEY (id);


--
-- Name: concurso concurso_loteria_id_numero_concurso_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.concurso
    ADD CONSTRAINT concurso_loteria_id_numero_concurso_key UNIQUE (loteria_id, numero_concurso);


--
-- Name: concurso_numero_sorteado concurso_numero_sorteado_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.concurso_numero_sorteado
    ADD CONSTRAINT concurso_numero_sorteado_pkey PRIMARY KEY (id);


--
-- Name: concurso concurso_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.concurso
    ADD CONSTRAINT concurso_pkey PRIMARY KEY (id);


--
-- Name: faixa_premiacao faixa_premiacao_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faixa_premiacao
    ADD CONSTRAINT faixa_premiacao_pkey PRIMARY KEY (id);


--
-- Name: ganhador ganhador_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ganhador
    ADD CONSTRAINT ganhador_pkey PRIMARY KEY (id);


--
-- Name: jogo jogo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.jogo
    ADD CONSTRAINT jogo_pkey PRIMARY KEY (id);


--
-- Name: loteria loteria_nome_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.loteria
    ADD CONSTRAINT loteria_nome_key UNIQUE (nome);


--
-- Name: loteria loteria_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.loteria
    ADD CONSTRAINT loteria_pkey PRIMARY KEY (id);


--
-- Name: probabilidade probabilidade_loteria_id_faixa_id_numeros_apostados_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.probabilidade
    ADD CONSTRAINT probabilidade_loteria_id_faixa_id_numeros_apostados_key UNIQUE (loteria_id, faixa_id, numeros_apostados);


--
-- Name: probabilidade probabilidade_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.probabilidade
    ADD CONSTRAINT probabilidade_pkey PRIMARY KEY (id);


--
-- Name: aposta_ganhadora aposta_ganhadora_concurso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aposta_ganhadora
    ADD CONSTRAINT aposta_ganhadora_concurso_id_fkey FOREIGN KEY (concurso_id) REFERENCES public.concurso(id) ON DELETE CASCADE;


--
-- Name: aposta_ganhadora aposta_ganhadora_faixa_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aposta_ganhadora
    ADD CONSTRAINT aposta_ganhadora_faixa_id_fkey FOREIGN KEY (faixa_id) REFERENCES public.faixa_premiacao(id);


--
-- Name: concurso concurso_loteria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.concurso
    ADD CONSTRAINT concurso_loteria_id_fkey FOREIGN KEY (loteria_id) REFERENCES public.loteria(id);


--
-- Name: concurso_numero_sorteado concurso_numero_sorteado_concurso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.concurso_numero_sorteado
    ADD CONSTRAINT concurso_numero_sorteado_concurso_id_fkey FOREIGN KEY (concurso_id) REFERENCES public.concurso(id) ON DELETE CASCADE;


--
-- Name: faixa_premiacao faixa_premiacao_loteria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faixa_premiacao
    ADD CONSTRAINT faixa_premiacao_loteria_id_fkey FOREIGN KEY (loteria_id) REFERENCES public.loteria(id);


--
-- Name: ganhador ganhador_concurso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ganhador
    ADD CONSTRAINT ganhador_concurso_id_fkey FOREIGN KEY (concurso_id) REFERENCES public.concurso(id) ON DELETE CASCADE;


--
-- Name: ganhador ganhador_faixa_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ganhador
    ADD CONSTRAINT ganhador_faixa_id_fkey FOREIGN KEY (faixa_id) REFERENCES public.faixa_premiacao(id);


--
-- Name: jogo jogo_concurso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.jogo
    ADD CONSTRAINT jogo_concurso_id_fkey FOREIGN KEY (concurso_id) REFERENCES public.concurso(id);


--
-- Name: jogo jogo_loteria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.jogo
    ADD CONSTRAINT jogo_loteria_id_fkey FOREIGN KEY (loteria_id) REFERENCES public.loteria(id);


--
-- Name: probabilidade probabilidade_faixa_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.probabilidade
    ADD CONSTRAINT probabilidade_faixa_id_fkey FOREIGN KEY (faixa_id) REFERENCES public.faixa_premiacao(id);


--
-- Name: probabilidade probabilidade_loteria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.probabilidade
    ADD CONSTRAINT probabilidade_loteria_id_fkey FOREIGN KEY (loteria_id) REFERENCES public.loteria(id);


--
-- PostgreSQL database dump complete
--

