import pymysql
import sys
import os

def import_sql_file(host, port, user, password, database, sql_filepath):
    print(f"Connecting to {host}:{port} as user '{user}'...")
    try:
        conn = pymysql.connect(
            host=host,
            port=int(port),
            user=user,
            password=password,
            database=database,
            ssl={'ssl': True},
            autocommit=True
        )
        print("Connected successfully!")
    except Exception as e:
        print(f"Error connecting to database: {e}")
        return False

    with open(sql_filepath, 'r', encoding='utf-8') as f:
        sql_content = f.read()

    # Split SQL file into statements
    statements = sql_content.split(';\n')
    cursor = conn.cursor()
    success_count = 0
    error_count = 0

    print(f"Executing SQL dump from {sql_filepath}...")
    for stmt in statements:
        stmt = stmt.strip()
        if not stmt or stmt.startswith('--') or stmt.startswith('/*'):
            continue
        try:
            cursor.execute(stmt)
            success_count += 1
        except Exception as err:
            print(f"Warning on statement execution: {err}")
            error_count += 1

    conn.close()
    print(f"Import complete! Statements executed: {success_count}, Warnings/Errors: {error_count}")
    return True

if __name__ == '__main__':
    password = sys.argv[1] if len(sys.argv) > 1 else os.getenv('SPRING_DATASOURCE_PASSWORD', '')
    host = os.getenv('SPRING_DATASOURCE_HOST', 'mysql-10811ce3-bhimashankarhugar2004-bc4c.c.aivencloud.com')
    port = int(os.getenv('SPRING_DATASOURCE_PORT', '28242'))
    user = os.getenv('SPRING_DATASOURCE_USERNAME', 'avnadmin')
    db = os.getenv('SPRING_DATASOURCE_DB', 'defaultdb')
    sql_file = os.path.join(os.path.dirname(__file__), 'plantify_data_dump.sql')

    import_sql_file(host, port, user, password, db, sql_file)
