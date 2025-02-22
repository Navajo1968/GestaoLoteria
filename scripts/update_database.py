import os
import time
import requests
import pandas as pd
import psycopg2
from psycopg2 import sql
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager

# Configurações do banco de dados
DB_HOST = "localhost"
DB_NAME = "gestaoloterias"
DB_USER = "postgres"
DB_PASSWORD = "@NaVaJo68#PostGre#"

# URL da página da Lotofácil
LOTTOFACIL_URL = "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx"
DOWNLOAD_PATH = "C:\\Users\\Rodrigo\\Downloads\\Lotofácil.xlsx"

def get_download_link():
    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
    
    driver = webdriver.Chrome(service=ChromeService(ChromeDriverManager().install()), options=chrome_options)
    
    try:
        driver.get(LOTTOFACIL_URL)
        wait = WebDriverWait(driver, 20)
        
        # Adiciona um tempo de espera para garantir que a página carregue completamente
        time.sleep(5)
        
        # Imprime o HTML da página carregada para depuração
        page_html = driver.page_source
        with open("page_source.html", "w", encoding="utf-8") as file:
            file.write(page_html)
        
        # Tenta encontrar o link de download usando diferentes seletores
        try:
            download_link = wait.until(EC.presence_of_element_located((By.LINK_TEXT, "Resultados da Lotofácil por ordem crescente")))
            file_url = download_link.get_attribute('href')
            return file_url
        except Exception as e:
            print(f"Erro ao tentar encontrar o link pelo texto: {e}")

        try:
            download_link = wait.until(EC.presence_of_element_located((By.XPATH, "//a[contains(text(), 'Resultados da Lotofácil por ordem crescente')]")))
            file_url = download_link.get_attribute('href')
            return file_url
        except Exception as e:
            print(f"Erro ao tentar encontrar o link pelo XPath: {e}")

        try:
            download_link = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "a[href*='download']")))
            file_url = download_link.get_attribute('href')
            return file_url
        except Exception as e:
            print(f"Erro ao tentar encontrar o link pelo CSS Selector: {e}")
        
        raise ValueError("Não foi possível encontrar o link de download")
    except Exception as e:
        print(f"Erro ao tentar encontrar o link: {e}")
        raise e
    finally:
        driver.quit()

def download_file(url, destination):
    if not url:
        raise ValueError("URL do arquivo é inválida")
    
    # Deletar o arquivo existente se houver
    if os.path.exists(destination):
        os.remove(destination)
    
    response = requests.get(url)
    response.raise_for_status()
    
    with open(destination, 'wb') as file:
        file.write(response.content)

def process_file(file_path):
    df = pd.read_excel(file_path, skiprows=1)  # Ajuste conforme necessário
    df = df.iloc[:, 1:17]  # Seleciona as colunas necessárias
    df.columns = ["dt_jogo", "num1", "num2", "num3", "num4", "num5", "num6", "num7", "num8", "num9", "num10", "num11", "num12", "num13", "num14", "num15"]
    return df

def update_database(df, id_loteria):
    conn = psycopg2.connect(
        host=DB_HOST,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )
    cur = conn.cursor()
    
    try:
        delete_query = sql.SQL("DELETE FROM tb_historico_jogos WHERE id_loterias = %s")
        cur.execute(delete_query, [id_loteria])
        
        insert_query = sql.SQL("""
            INSERT INTO tb_historico_jogos (id_loterias, dt_jogo, num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """)
        
        for _, row in df.iterrows():
            cur.execute(insert_query, [id_loteria] + row.tolist())
        
        conn.commit()
    except Exception as e:
        conn.rollback()
        raise e
    finally:
        cur.close()
        conn.close()

def main():
    try:
        file_url = get_download_link()
        print(f"URL do arquivo: {file_url}")
        download_file(file_url, DOWNLOAD_PATH)
        
        df = process_file(DOWNLOAD_PATH)
        # ID da loteria, ajuste conforme necessário
        id_loteria = 1
        update_database(df, id_loteria)
        
        print("Base de dados atualizada com sucesso.")
    except Exception as e:
        print(f"Erro: {e}")

if __name__ == "__main__":
    main()