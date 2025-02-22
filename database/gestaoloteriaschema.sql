--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

-- Started on 2025-02-21 23:44:42

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

--
-- TOC entry 4913 (class 1262 OID 16412)
-- Name: gestaoloterias; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE gestaoloterias WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Portuguese_Brazil.1252';


ALTER DATABASE gestaoloterias OWNER TO postgres;

\connect gestaoloterias

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

--
-- TOC entry 4914 (class 0 OID 0)
-- Dependencies: 4913
-- Name: DATABASE gestaoloterias; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON DATABASE gestaoloterias IS 'Irá armazenar os resultado dos jogos das loterias';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 241 (class 1259 OID 16807)
-- Name: tb_analise_concursos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_analise_concursos (
    id_analise integer NOT NULL,
    id_loterias integer NOT NULL,
    nr_concurso integer NOT NULL,
    tipo_metrica character varying(50) NOT NULL,
    valor double precision NOT NULL,
    dt_analise timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.tb_analise_concursos OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 16806)
-- Name: tb_analise_concursos_id_analise_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_analise_concursos_id_analise_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_analise_concursos_id_analise_seq OWNER TO postgres;

--
-- TOC entry 4915 (class 0 OID 0)
-- Dependencies: 240
-- Name: tb_analise_concursos_id_analise_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_analise_concursos_id_analise_seq OWNED BY public.tb_analise_concursos.id_analise;


--
-- TOC entry 233 (class 1259 OID 16751)
-- Name: tb_analise_estatistica; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_analise_estatistica (
    id_analise integer NOT NULL,
    id_loterias integer NOT NULL,
    num_analisado integer NOT NULL,
    frequencia integer NOT NULL,
    recencia double precision NOT NULL,
    media_posicao double precision NOT NULL,
    dt_analise timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.tb_analise_estatistica OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 16750)
-- Name: tb_analise_estatistica_id_analise_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_analise_estatistica_id_analise_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_analise_estatistica_id_analise_seq OWNER TO postgres;

--
-- TOC entry 4916 (class 0 OID 0)
-- Dependencies: 232
-- Name: tb_analise_estatistica_id_analise_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_analise_estatistica_id_analise_seq OWNED BY public.tb_analise_estatistica.id_analise;


--
-- TOC entry 217 (class 1259 OID 16413)
-- Name: tb_historico_jogos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_historico_jogos (
    dt_jogo date NOT NULL,
    num1 integer,
    num2 integer,
    num3 integer,
    num4 integer,
    num5 integer,
    num6 integer,
    num7 integer,
    num8 integer,
    num9 integer,
    num10 integer,
    num11 integer,
    num12 integer,
    num13 integer,
    num14 integer,
    num15 integer,
    id_loterias integer DEFAULT 1 NOT NULL,
    num_concurso integer
);


ALTER TABLE public.tb_historico_jogos OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16424)
-- Name: tb_jogos_gerados; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_jogos_gerados (
    id_gerados integer NOT NULL,
    dt_inclusao timestamp with time zone DEFAULT now() NOT NULL,
    dt_aposta date,
    num_concurso integer,
    num1 integer,
    num2 integer,
    num3 integer,
    num4 integer,
    num5 integer,
    num6 integer,
    num7 integer,
    num8 integer,
    num9 integer,
    num10 integer,
    num11 integer,
    num12 integer,
    num13 integer,
    num14 integer,
    num15 integer,
    tot_acertos integer,
    id_loterias integer DEFAULT 1 NOT NULL,
    dt_concurso date,
    numero_jogo integer,
    numeros text,
    num16 integer,
    num17 integer,
    num18 integer,
    num19 integer,
    num20 integer
);


ALTER TABLE public.tb_jogos_gerados OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16423)
-- Name: tb_jogos_gerados_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_jogos_gerados_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_jogos_gerados_id_seq OWNER TO postgres;

--
-- TOC entry 4917 (class 0 OID 0)
-- Dependencies: 218
-- Name: tb_jogos_gerados_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_jogos_gerados_id_seq OWNED BY public.tb_jogos_gerados.id_gerados;


--
-- TOC entry 221 (class 1259 OID 16450)
-- Name: tb_loterias; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_loterias (
    id_loterias integer NOT NULL,
    des_nome character varying(40),
    des_contexto character varying(1000),
    qt_numeros integer,
    dt_inclusao date,
    seg boolean,
    ter boolean,
    qua boolean,
    qui boolean,
    sex boolean,
    sab boolean,
    dom boolean,
    horario_sorteio time without time zone
);


ALTER TABLE public.tb_loterias OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16449)
-- Name: tb_loterias_id_loterias_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_loterias_id_loterias_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_loterias_id_loterias_seq OWNER TO postgres;

--
-- TOC entry 4918 (class 0 OID 0)
-- Dependencies: 220
-- Name: tb_loterias_id_loterias_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_loterias_id_loterias_seq OWNED BY public.tb_loterias.id_loterias;


--
-- TOC entry 223 (class 1259 OID 16459)
-- Name: tb_loterias_preco; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_loterias_preco (
    id_loterias integer NOT NULL,
    id_loterias_preco integer NOT NULL,
    qt_numeros_jogados integer,
    vlr_aposta numeric
);


ALTER TABLE public.tb_loterias_preco OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16458)
-- Name: tb_loterias_preco_id_loterias_preco_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_loterias_preco_id_loterias_preco_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_loterias_preco_id_loterias_preco_seq OWNER TO postgres;

--
-- TOC entry 4919 (class 0 OID 0)
-- Dependencies: 222
-- Name: tb_loterias_preco_id_loterias_preco_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_loterias_preco_id_loterias_preco_seq OWNED BY public.tb_loterias_preco.id_loterias_preco;


--
-- TOC entry 225 (class 1259 OID 16474)
-- Name: tb_loterias_probabilidade; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_loterias_probabilidade (
    id_loterias integer NOT NULL,
    id_loterias_preco integer NOT NULL,
    id_loterias_probabilidade integer NOT NULL,
    qt_numeros_acertos integer,
    qt_numeros_jogados integer,
    qt_probabilidade integer,
    vlr_fator_premiacao numeric
);


ALTER TABLE public.tb_loterias_probabilidade OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16473)
-- Name: tb_loterias_probabilidade_id_loterias_probabilidade_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_loterias_probabilidade_id_loterias_probabilidade_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_loterias_probabilidade_id_loterias_probabilidade_seq OWNER TO postgres;

--
-- TOC entry 4920 (class 0 OID 0)
-- Dependencies: 224
-- Name: tb_loterias_probabilidade_id_loterias_probabilidade_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_loterias_probabilidade_id_loterias_probabilidade_seq OWNED BY public.tb_loterias_probabilidade.id_loterias_probabilidade;


--
-- TOC entry 227 (class 1259 OID 16528)
-- Name: tb_lotofacil_resultados; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_lotofacil_resultados (
    id integer NOT NULL,
    numero_concurso integer NOT NULL,
    data_concurso date NOT NULL,
    data_proximo_concurso date,
    acumulado boolean,
    dezenas_sorteadas_ordem_sorteio character varying(3)[],
    lista_dezenas character varying(3)[],
    exibir_detalhamento_por_cidade boolean,
    indicador_concurso_especial integer,
    lista_municipio_uf_ganhadores jsonb,
    lista_rateio_premio jsonb,
    local_sorteio character varying(255),
    nome_municipio_uf_sorteio character varying(255),
    nome_time_coracao_mes_sorte character varying(255),
    numero_concurso_anterior integer,
    numero_concurso_final_0_5 integer,
    numero_concurso_proximo integer,
    numero_jogo integer,
    observacao text,
    tipo_jogo character varying(50),
    tipo_publicacao integer,
    ultimo_concurso boolean,
    valor_arrecadado numeric,
    valor_acumulado_concurso_0_5 numeric,
    valor_acumulado_concurso_especial numeric,
    valor_acumulado_proximo_concurso numeric,
    valor_estimado_proximo_concurso numeric,
    valor_saldo_reserva_garantidora numeric,
    valor_total_premio_faixa_um numeric,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.tb_lotofacil_resultados OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16527)
-- Name: tb_lotofacil_resultados_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_lotofacil_resultados_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_lotofacil_resultados_id_seq OWNER TO postgres;

--
-- TOC entry 4921 (class 0 OID 0)
-- Dependencies: 226
-- Name: tb_lotofacil_resultados_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_lotofacil_resultados_id_seq OWNED BY public.tb_lotofacil_resultados.id;


--
-- TOC entry 231 (class 1259 OID 16739)
-- Name: tb_metricas_jogos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_metricas_jogos (
    id_metrica integer NOT NULL,
    id_loterias integer,
    id_gerados integer,
    score_paridade numeric(5,2),
    score_distribuicao numeric(5,2),
    score_historico numeric(5,2),
    score_final numeric(5,2),
    dt_avaliacao timestamp without time zone
);


ALTER TABLE public.tb_metricas_jogos OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16738)
-- Name: tb_metricas_jogos_id_metrica_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_metricas_jogos_id_metrica_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_metricas_jogos_id_metrica_seq OWNER TO postgres;

--
-- TOC entry 4922 (class 0 OID 0)
-- Dependencies: 230
-- Name: tb_metricas_jogos_id_metrica_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_metricas_jogos_id_metrica_seq OWNED BY public.tb_metricas_jogos.id_metrica;


--
-- TOC entry 235 (class 1259 OID 16764)
-- Name: tb_metricas_qualidade; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_metricas_qualidade (
    id_metrica integer NOT NULL,
    id_loterias integer NOT NULL,
    tipo_metrica character varying(50) NOT NULL,
    valor double precision NOT NULL,
    dt_calculo timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.tb_metricas_qualidade OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 16763)
-- Name: tb_metricas_qualidade_id_metrica_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_metricas_qualidade_id_metrica_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_metricas_qualidade_id_metrica_seq OWNER TO postgres;

--
-- TOC entry 4923 (class 0 OID 0)
-- Dependencies: 234
-- Name: tb_metricas_qualidade_id_metrica_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_metricas_qualidade_id_metrica_seq OWNED BY public.tb_metricas_qualidade.id_metrica;


--
-- TOC entry 229 (class 1259 OID 16667)
-- Name: tb_padroes_frequentes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_padroes_frequentes (
    id_padrao integer NOT NULL,
    id_loterias integer,
    tipo_padrao character varying(50),
    padrao text,
    frequencia integer,
    probabilidade numeric(10,8),
    ultima_ocorrencia timestamp without time zone
);


ALTER TABLE public.tb_padroes_frequentes OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16666)
-- Name: tb_padroes_frequentes_id_padrao_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_padroes_frequentes_id_padrao_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_padroes_frequentes_id_padrao_seq OWNER TO postgres;

--
-- TOC entry 4924 (class 0 OID 0)
-- Dependencies: 228
-- Name: tb_padroes_frequentes_id_padrao_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_padroes_frequentes_id_padrao_seq OWNED BY public.tb_padroes_frequentes.id_padrao;


--
-- TOC entry 237 (class 1259 OID 16777)
-- Name: tb_padroes_identificados; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_padroes_identificados (
    id_padrao integer NOT NULL,
    id_loterias integer NOT NULL,
    nome_padrao character varying(50) NOT NULL,
    valor double precision NOT NULL,
    dt_identificacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.tb_padroes_identificados OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 16776)
-- Name: tb_padroes_identificados_id_padrao_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_padroes_identificados_id_padrao_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_padroes_identificados_id_padrao_seq OWNER TO postgres;

--
-- TOC entry 4925 (class 0 OID 0)
-- Dependencies: 236
-- Name: tb_padroes_identificados_id_padrao_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_padroes_identificados_id_padrao_seq OWNED BY public.tb_padroes_identificados.id_padrao;


--
-- TOC entry 239 (class 1259 OID 16790)
-- Name: tb_pesos_numeros; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tb_pesos_numeros (
    id_peso integer NOT NULL,
    id_loterias integer NOT NULL,
    num_analisado integer NOT NULL,
    peso double precision NOT NULL,
    dt_calculo timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.tb_pesos_numeros OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 16789)
-- Name: tb_pesos_numeros_id_peso_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tb_pesos_numeros_id_peso_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tb_pesos_numeros_id_peso_seq OWNER TO postgres;

--
-- TOC entry 4926 (class 0 OID 0)
-- Dependencies: 238
-- Name: tb_pesos_numeros_id_peso_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tb_pesos_numeros_id_peso_seq OWNED BY public.tb_pesos_numeros.id_peso;


--
-- TOC entry 4719 (class 2604 OID 16810)
-- Name: tb_analise_concursos id_analise; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_analise_concursos ALTER COLUMN id_analise SET DEFAULT nextval('public.tb_analise_concursos_id_analise_seq'::regclass);


--
-- TOC entry 4711 (class 2604 OID 16754)
-- Name: tb_analise_estatistica id_analise; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_analise_estatistica ALTER COLUMN id_analise SET DEFAULT nextval('public.tb_analise_estatistica_id_analise_seq'::regclass);


--
-- TOC entry 4701 (class 2604 OID 16737)
-- Name: tb_jogos_gerados id_gerados; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_jogos_gerados ALTER COLUMN id_gerados SET DEFAULT nextval('public.tb_jogos_gerados_id_seq'::regclass);


--
-- TOC entry 4704 (class 2604 OID 16453)
-- Name: tb_loterias id_loterias; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias ALTER COLUMN id_loterias SET DEFAULT nextval('public.tb_loterias_id_loterias_seq'::regclass);


--
-- TOC entry 4705 (class 2604 OID 16462)
-- Name: tb_loterias_preco id_loterias_preco; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias_preco ALTER COLUMN id_loterias_preco SET DEFAULT nextval('public.tb_loterias_preco_id_loterias_preco_seq'::regclass);


--
-- TOC entry 4706 (class 2604 OID 16477)
-- Name: tb_loterias_probabilidade id_loterias_probabilidade; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias_probabilidade ALTER COLUMN id_loterias_probabilidade SET DEFAULT nextval('public.tb_loterias_probabilidade_id_loterias_probabilidade_seq'::regclass);


--
-- TOC entry 4707 (class 2604 OID 16531)
-- Name: tb_lotofacil_resultados id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_lotofacil_resultados ALTER COLUMN id SET DEFAULT nextval('public.tb_lotofacil_resultados_id_seq'::regclass);


--
-- TOC entry 4710 (class 2604 OID 16742)
-- Name: tb_metricas_jogos id_metrica; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_metricas_jogos ALTER COLUMN id_metrica SET DEFAULT nextval('public.tb_metricas_jogos_id_metrica_seq'::regclass);


--
-- TOC entry 4713 (class 2604 OID 16767)
-- Name: tb_metricas_qualidade id_metrica; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_metricas_qualidade ALTER COLUMN id_metrica SET DEFAULT nextval('public.tb_metricas_qualidade_id_metrica_seq'::regclass);


--
-- TOC entry 4709 (class 2604 OID 16670)
-- Name: tb_padroes_frequentes id_padrao; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_padroes_frequentes ALTER COLUMN id_padrao SET DEFAULT nextval('public.tb_padroes_frequentes_id_padrao_seq'::regclass);


--
-- TOC entry 4715 (class 2604 OID 16780)
-- Name: tb_padroes_identificados id_padrao; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_padroes_identificados ALTER COLUMN id_padrao SET DEFAULT nextval('public.tb_padroes_identificados_id_padrao_seq'::regclass);


--
-- TOC entry 4717 (class 2604 OID 16793)
-- Name: tb_pesos_numeros id_peso; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_pesos_numeros ALTER COLUMN id_peso SET DEFAULT nextval('public.tb_pesos_numeros_id_peso_seq'::regclass);


--
-- TOC entry 4728 (class 2606 OID 16466)
-- Name: tb_loterias_preco pk_loterias_preco; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias_preco
    ADD CONSTRAINT pk_loterias_preco PRIMARY KEY (id_loterias, id_loterias_preco);


--
-- TOC entry 4730 (class 2606 OID 16479)
-- Name: tb_loterias_probabilidade pk_loterias_probabilidade; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias_probabilidade
    ADD CONSTRAINT pk_loterias_probabilidade PRIMARY KEY (id_loterias, id_loterias_preco, id_loterias_probabilidade);


--
-- TOC entry 4751 (class 2606 OID 16813)
-- Name: tb_analise_concursos tb_analise_concursos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_analise_concursos
    ADD CONSTRAINT tb_analise_concursos_pkey PRIMARY KEY (id_analise);


--
-- TOC entry 4739 (class 2606 OID 16757)
-- Name: tb_analise_estatistica tb_analise_estatistica_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_analise_estatistica
    ADD CONSTRAINT tb_analise_estatistica_pkey PRIMARY KEY (id_analise);


--
-- TOC entry 4722 (class 2606 OID 16487)
-- Name: tb_historico_jogos tb_historico_jogos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_historico_jogos
    ADD CONSTRAINT tb_historico_jogos_pkey PRIMARY KEY (id_loterias, dt_jogo);


--
-- TOC entry 4724 (class 2606 OID 16508)
-- Name: tb_jogos_gerados tb_jogos_gerados_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_jogos_gerados
    ADD CONSTRAINT tb_jogos_gerados_pkey PRIMARY KEY (id_loterias, id_gerados);


--
-- TOC entry 4726 (class 2606 OID 16457)
-- Name: tb_loterias tb_loterias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias
    ADD CONSTRAINT tb_loterias_pkey PRIMARY KEY (id_loterias);


--
-- TOC entry 4732 (class 2606 OID 16536)
-- Name: tb_lotofacil_resultados tb_lotofacil_resultados_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_lotofacil_resultados
    ADD CONSTRAINT tb_lotofacil_resultados_pkey PRIMARY KEY (id);


--
-- TOC entry 4736 (class 2606 OID 16744)
-- Name: tb_metricas_jogos tb_metricas_jogos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_metricas_jogos
    ADD CONSTRAINT tb_metricas_jogos_pkey PRIMARY KEY (id_metrica);


--
-- TOC entry 4742 (class 2606 OID 16770)
-- Name: tb_metricas_qualidade tb_metricas_qualidade_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_metricas_qualidade
    ADD CONSTRAINT tb_metricas_qualidade_pkey PRIMARY KEY (id_metrica);


--
-- TOC entry 4734 (class 2606 OID 16674)
-- Name: tb_padroes_frequentes tb_padroes_frequentes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_padroes_frequentes
    ADD CONSTRAINT tb_padroes_frequentes_pkey PRIMARY KEY (id_padrao);


--
-- TOC entry 4745 (class 2606 OID 16783)
-- Name: tb_padroes_identificados tb_padroes_identificados_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_padroes_identificados
    ADD CONSTRAINT tb_padroes_identificados_pkey PRIMARY KEY (id_padrao);


--
-- TOC entry 4748 (class 2606 OID 16796)
-- Name: tb_pesos_numeros tb_pesos_numeros_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_pesos_numeros
    ADD CONSTRAINT tb_pesos_numeros_pkey PRIMARY KEY (id_peso);


--
-- TOC entry 4749 (class 1259 OID 16819)
-- Name: idx_analise_concursos_loteria_concurso; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_analise_concursos_loteria_concurso ON public.tb_analise_concursos USING btree (id_loterias, nr_concurso);


--
-- TOC entry 4737 (class 1259 OID 16802)
-- Name: idx_analise_loteria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_analise_loteria ON public.tb_analise_estatistica USING btree (id_loterias);


--
-- TOC entry 4740 (class 1259 OID 16803)
-- Name: idx_metricas_loteria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_metricas_loteria ON public.tb_metricas_qualidade USING btree (id_loterias);


--
-- TOC entry 4743 (class 1259 OID 16804)
-- Name: idx_padroes_loteria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_padroes_loteria ON public.tb_padroes_identificados USING btree (id_loterias);


--
-- TOC entry 4746 (class 1259 OID 16805)
-- Name: idx_pesos_loteria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pesos_loteria ON public.tb_pesos_numeros USING btree (id_loterias);


--
-- TOC entry 4752 (class 2606 OID 16488)
-- Name: tb_historico_jogos fk_historico_jogos_loterias; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_historico_jogos
    ADD CONSTRAINT fk_historico_jogos_loterias FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4753 (class 2606 OID 16502)
-- Name: tb_jogos_gerados fk_jogos_gerados_loterias; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_jogos_gerados
    ADD CONSTRAINT fk_jogos_gerados_loterias FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4754 (class 2606 OID 16467)
-- Name: tb_loterias_preco fk_loterias; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias_preco
    ADD CONSTRAINT fk_loterias FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4755 (class 2606 OID 16480)
-- Name: tb_loterias_probabilidade fk_loterias_preco; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_loterias_probabilidade
    ADD CONSTRAINT fk_loterias_preco FOREIGN KEY (id_loterias, id_loterias_preco) REFERENCES public.tb_loterias_preco(id_loterias, id_loterias_preco) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4762 (class 2606 OID 16814)
-- Name: tb_analise_concursos tb_analise_concursos_id_loterias_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_analise_concursos
    ADD CONSTRAINT tb_analise_concursos_id_loterias_fkey FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias);


--
-- TOC entry 4758 (class 2606 OID 16758)
-- Name: tb_analise_estatistica tb_analise_estatistica_id_loterias_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_analise_estatistica
    ADD CONSTRAINT tb_analise_estatistica_id_loterias_fkey FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias);


--
-- TOC entry 4757 (class 2606 OID 16745)
-- Name: tb_metricas_jogos tb_metricas_jogos_id_loterias_id_gerados_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_metricas_jogos
    ADD CONSTRAINT tb_metricas_jogos_id_loterias_id_gerados_fkey FOREIGN KEY (id_loterias, id_gerados) REFERENCES public.tb_jogos_gerados(id_loterias, id_gerados);


--
-- TOC entry 4759 (class 2606 OID 16771)
-- Name: tb_metricas_qualidade tb_metricas_qualidade_id_loterias_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_metricas_qualidade
    ADD CONSTRAINT tb_metricas_qualidade_id_loterias_fkey FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias);


--
-- TOC entry 4756 (class 2606 OID 16675)
-- Name: tb_padroes_frequentes tb_padroes_frequentes_id_loterias_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_padroes_frequentes
    ADD CONSTRAINT tb_padroes_frequentes_id_loterias_fkey FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias);


--
-- TOC entry 4760 (class 2606 OID 16784)
-- Name: tb_padroes_identificados tb_padroes_identificados_id_loterias_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_padroes_identificados
    ADD CONSTRAINT tb_padroes_identificados_id_loterias_fkey FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias);


--
-- TOC entry 4761 (class 2606 OID 16797)
-- Name: tb_pesos_numeros tb_pesos_numeros_id_loterias_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tb_pesos_numeros
    ADD CONSTRAINT tb_pesos_numeros_id_loterias_fkey FOREIGN KEY (id_loterias) REFERENCES public.tb_loterias(id_loterias);


-- Completed on 2025-02-21 23:44:43

--
-- PostgreSQL database dump complete
--

