import gymnasium as gym
from gymnasium import spaces
import numpy as np
import networkx as nx
from sb3_contrib import MaskablePPO
from sb3_contrib.common.wrappers import ActionMasker
from stable_baselines3 import PPO
import paramiko
import subprocess
import os


# Linux上にあるファイルと .exe のパス
local_xml = "./tree_xml/g62_-977133682.xml"
exe_path = "../hexcore5/hexcore5_niccolo.exe" # Linuxサーバー上に.exeをコピーしておく

exe_dir = os.path.abspath("../hexcore5") 
exe_path = os.path.join(exe_dir, "hexcore5_niccolo.exe")

my_env = os.environ.copy()
my_env["WINEDEBUG"] = "-all"

# wineコマンドを使って実行
command = ["wine", exe_path, local_xml]

print(f"Executing: {' '.join(command)}")

# 実行して結果を待つ
result = subprocess.run(command, capture_output=True, text=True, env=my_env, cwd=exe_dir)

if result.stdout:
    print("STDOUT:", result.stdout.strip())
if result.stderr:
    print("STDERR:", result.stderr.strip())

print("処理完了！")


















# hostname = "10.7.1.34"  # Windows PCのIPアドレス
# username = "r2d2\piros"
# #password = "your_password"

# local_file = "./tree_xml/-65837229546948026.xml"               # Linux側のファイルパス
# remote_path = "C:/Users/piros/Documents/nedo_futami/nedo2024/src/reinforcement_learning/tree_xml/"       # Windows側の転送先ディレクトリ
# remote_file = remote_path + local_file.split("/")[-1] # Windows側のファイルフルパス
# exe_path = "C:/Users/piros/Documents/nedo_futami/hexcore5/hexcore5_niccolo.exe"    # 実行ファイルのパス
# KEY_PATH = "/workspace/.ssh/id_rsa" 
# ssh = paramiko.SSHClient()
# ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
# private_key = paramiko.RSAKey.from_private_key_file(KEY_PATH)
# ssh.connect(
#     hostname=hostname,
#     port=22,
#     username=username,
#     pkey=private_key
# )

# sftp = ssh.open_sftp()
# sftp.put(local_file, remote_file)
# sftp.close()

# 3. .exe の実行 (引数に data.xml を指定)
# Windowsのコマンドプロンプト形式で実行コマンドを作成
# 引数にパスが含まれる場合はダブルクォーテーションで囲むのが安全です
# command = f'"{exe_path}" "{remote_file}"'
# print(f"Executing: {command}")

# stdin, stdout, stderr = ssh.exec_command(command)

# # 実行結果の確認（標準出力とエラー出力）
# out = stdout.read().decode('cp932') # Windowsは通常cp932(SJIS)
# err = stderr.read().decode('cp932')

# if out: print(f"STDOUT: {out}")
# if err: print(f"STDERR: {err}")

# 4. 処理されたファイルを戻す (SFTP)
# 必要に応じてローカルのファイル名を変える（例: data_processed.xml）
# print("Fetching the result back to Linux...")
# sftp = ssh.open_sftp()
# sftp.get(remote_file, )
# sftp.close()

