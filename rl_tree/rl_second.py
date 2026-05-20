import sys
import torch
import torch.nn as nn
import gymnasium as gym
from gymnasium import spaces
import numpy as np
import networkx as nx
from sb3_contrib import MaskablePPO
from sb3_contrib.common.wrappers import ActionMasker
from stable_baselines3 import PPO
from torch_geometric.nn import GCNConv, global_mean_pool
from stable_baselines3.common.torch_layers import BaseFeaturesExtractor
#import paramiko
import subprocess
import os

tree_xmlpath = "C:\\Users\\piros\\Documents\\nedo_futami\\rl_tree\\tree_xml_second"
folderName = "C:\\Users\\piros\\Documents\\nedo_futami\\hexcore5"
exeFileName = "hexcore5_niccolo.exe"

tempFolderName = "C:\\Users\\piros\\Documents\\nedo_futami\\nedo"

gnnfolder = tempFolderName + "\\gnn\\false_0_add"

class GNNFeaturesExtractor(BaseFeaturesExtractor):
    def __init__(self, observation_space, features_dim=128):
        # 最終的にPPOに渡す特徴量サイズを128に設定
        super().__init__(observation_space, features_dim)
        
        node_input_dim = 3  # (x, y, z(input direction)) 座標
        self.conv1 = GCNConv(node_input_dim, 30)
        self.conv2 = GCNConv(30, 30)
        self.conv3 = GCNConv(30, 30)
        self.conv4 = GCNConv(30, 30)
        self.fc = nn.Linear(30, features_dim)

    def forward(self, observations):
        # observations["nodes"]: (batch, num_nodes, 3)
        # observations["edges"]: (batch, num_nodes, num_nodes)
        
        x_batch = observations["nodes"]
        adj_batch = observations["edges"]
        
        batch_size = x_batch.shape[0]
        out_features = []
        
        # バッチ内のグラフを1つずつ処理する
        for i in range(batch_size):
            x = x_batch[i]
            adj = adj_batch[i]
            
            # 隣接行列からエッジリストへ変換 (PyTorch Geometric用)
            edge_index = adj.nonzero(as_tuple=False).t().contiguous()
            
            # GNN層を適用
            h = torch.relu(self.conv1(x, edge_index))
            h = torch.relu(self.conv2(h, edge_index))
            h = torch.relu(self.conv3(h, edge_index))
            h = torch.relu(self.conv4(h, edge_index))
            # 各グラフの情報を平均して 1つのベクトル (1, 128) に集約
            pooled = h.mean(dim=0, keepdim=True)
            out_features.append(pooled)
            
        # リストを結合して (batch_size, features_dim) のテンソルにする
        batch_pooled = torch.cat(out_features, dim=0)
        
        # 最後に全結合層を通して返す
        return self.fc(batch_pooled)
    
class TreeBuilderEnv(gym.Env):
    """閉路を作らずに木(Tree)を構築する強化学習環境"""
    def __init__(self, row_num=12, column_num=3):
        super(TreeBuilderEnv, self).__init__()
        self.row_num = row_num
        self.column_num = column_num
        self.num_nodes = row_num * column_num
        self.children = [[] for _ in range(self.num_nodes)]
        # ノードごとの (row, col) 座標を正規化して保持
        self.node_features = np.zeros((self.num_nodes, 3), dtype=np.float32)
        for i in range(self.num_nodes):
            self.node_features[i, 0] = i % self.row_num
            self.node_features[i, 1] = i // self.row_num
            self.node_features[i, 2] = 0  # z座標を0で初期化
        self.action_space = spaces.Discrete(self.num_nodes * self.num_nodes)  # 頂点uとvを選ぶ行動空間
        self.observation_space = spaces.Dict({
            "nodes": spaces.Box(low=0, high=1, shape=(self.num_nodes, 3), dtype=np.float32),
            "edges": spaces.Box(low=0, high=1, shape=(self.num_nodes, self.num_nodes), dtype=np.float32)
        })
        # self.observation_space = spaces.Box(
        #     low=0, high=1, 
        #     shape=(self.num_nodes * self.num_nodes,), 
        #     dtype=np.int8
        # )


    def reset(self, seed=None, options=None):
        super().reset(seed=seed)
        self.adj_matrix = np.zeros((self.num_nodes, self.num_nodes), dtype=np.int8)
        self.step_count = 0
        self.edge_count = 0  # 現在引かれている辺の数
        self.children = [[] for _ in range(self.num_nodes)]
        obs = {
            "nodes": self.node_features,
            "edges": self.adj_matrix.astype(np.float32)
        }
        return obs, {}
        return self.adj_matrix.flatten(), {}

    def valid_action_mask(self):
        valid_actions = np.zeros(self.num_nodes * self.num_nodes, dtype=bool)
        
        # 各ノードの出次数（出ている辺の数）と入次数（入っている辺の数）
        out_degrees = np.sum(self.adj_matrix, axis=1)
        in_degrees = np.sum(self.adj_matrix, axis=0)
        
        # 接続済みかどうか（出ているか入っている辺が1つ以上ある）
        connected = (out_degrees + in_degrees) > 0

        # 1つの分岐点から出せる管の最大数
        MAX_CHILDREN = 2 
        # 分岐しているノードの数
        split_count = 0
        # 接続済みノードの数
        connected_count = np.sum(connected)

        for deg in out_degrees:
            if deg > 1:
                split_count += 1
        
        for action in range(self.num_nodes * self.num_nodes):
            u = action // self.num_nodes
            v = action % self.num_nodes

            # 自分自身はNG
            if u == v:
                continue
                
            if self.edge_count == 0:
                # 1手目: どこからスタートしてもOK
                valid_actions[action] = True
            else:
                # 2手目以降のルール
                
                # ルール1: v（繋がれる側）はまだグラフに無い「新しいノード」であること
                if connected[v]:
                    continue
                    
                # ルール2: u（繋ぐ元）はすでにグラフにある「接続済みノード」であること
                if not connected[u]:
                    continue
                    
                # ルール3: u（繋ぐ元）の枝の数が上限に達していないこと
            
                if out_degrees[u] >= MAX_CHILDREN:
                    continue

                if out_degrees[u] >= 1:
                    if split_count >= 1:
                    # すでに1本出ているノードからさらに分岐させるのは、全体で1回まで
                        continue
                    # if connected_count %2 == 1:
                    #     continue

                
                # 全ての条件をクリアしたものだけ有効
                valid_actions[action] = True

        return valid_actions

    def step(self, action):
        self.step_count += 1
        u = action // self.num_nodes
        v = action % self.num_nodes

        reward = 0
        terminated = False
        truncated = False

        # 現在のグラフ状態をNetworkXに変換
        G = nx.from_numpy_array(self.adj_matrix)

        if u == v:
            # 自分自身を繋ぐ行動は無効
            reward = -10.0
        elif self.adj_matrix[u, v] == 1:
            # すでに繋がっている辺を再度選ぶのも無効
            reward = -5.0
        elif nx.has_path(G, u, v):
            # ★重要★ すでに別の経路で繋がっている頂点同士を繋ぐと「閉路」ができるため強いペナルティ
            reward = -10.0
        else:
            # 閉路を作らない有効な接続（まだ繋がっていないコンポーネント同士の接続）有向木を構築するため、片方向のみ接続
            self.adj_matrix[u, v] = 1
            self.children[u].append(v)  # uの子供にvを追加
            if self.edge_count == 0:
                self.node_features[u, 2] = 1  # 最初のノードはz座標を1にしてスタート
                self.node_features[v, 2] = 2  # 前のノードと反対になる
            else :
                if self.node_features[u, 2] == 1:
                    self.node_features[v, 2] = 2  # 
                elif self.node_features[u, 2] == 2:
                    self.node_features[v, 2] = 1  # 
            #self.adj_matrix[v, u] = 1
            self.edge_count += 1
            

            # 辺の数が (頂点数 - 1) になったら「木」の完成
            if self.edge_count == self.num_nodes - 1:

                terminated = True
                state_tuple = tuple(tuple(children) for children in self.children)
                current_hash = hash(state_tuple) 
                create_xml(f"{current_hash}.xml", self.row_num, self.column_num, self.children)

                local_file = os.path.join(tree_xmlpath, f"{current_hash}.xml")
                exe_path = os.path.join(folderName, exeFileName)
                command = ["cmd", "/c", exeFileName, local_file]  # コマンドと引数のリスト

                # プロセスを起動 (Javaの Process p = runtime.exec(...) に相当)
                # p = subprocess.Popen(command, stdout=subprocess.PIPE, text=True,cwd=folderName)

                # # 2. communicate() でプロセスの終了を待ちつつ、全出力を一括取得
                # stdout_data, stderr_data = p.communicate()
                # print(f"Javaの出力: {stdout_data.strip()}")  # Javaからの出力を表示
                # val = 0.0

                # # 3. クラッシュ時の安全対策
                # if p.returncode != 0:
                #     print(f"実行エラー (終了コード: {p.returncode})", file=sys.stderr)
                #     val = -1.0
                # else:
                #     stdout_data = stdout_data.strip()
                #     val= float(stdout_data)
                try:
        # プロセスの実行と出力の取得
                    p = subprocess.Popen(
                        command,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE,
                        stdin=subprocess.DEVNULL,
                        text=True,
                        cwd=folderName
                    )

                    
                    stdout_data, stderr_data = p.communicate(timeout=30)
        
                   
                    
                    # 出力を改行で分割（Javaの readLine() に相当）
                    lines = stdout_data.splitlines()

                    val = 0.0
                    target_line = None

                    # Javaの「とりあえず変換してみる」処理を再現
                    try:
                        # Javaの line = br.readLine(); line = br.readLine(); に相当する部分
                        # もし2行目が無ければ（出力が空など）、ここで IndexError が発生して except へ飛ぶ
                        target_line = lines[1] if len(lines) > 1 else lines[0]
                        
                        # 数値への変換。文字が含まれていたり空文字なら ValueError が発生して except へ飛ぶ
                        val = float(target_line)

                    except Exception:
                        # ======= ここが Javaの catch (Exception e) の中身と同じ =======
                        if target_line is not None and "INFEASIBLE" in target_line:
                            print(f"INFEASIBLE: {target_line}", file=sys.stderr)
                        elif target_line is not None and "Invalid" in target_line:
                            print(f"Invalid: {target_line}", file=sys.stderr)
                        elif target_line is not None and target_line.strip() != "":
                            print(target_line)
                        
                        # どんなエラーが起きても最終的に -1 を返す
                        val = -5.0

                except Exception as e:
                    # Popen 自体が失敗した（ファイルがない等）場合の最終安全網
                    print(f"System Error: {e}", file=sys.stderr)
                    val = -5.0

                reward += val  # Javaの reward += val に相当
                truncated = True
                print(f"Reward: {reward}")


        # 探索が長引いた場合は打ち切り
        if self.step_count > self.num_nodes :
            truncated = True

        obs = {
            "nodes": self.node_features,
            "edges": self.adj_matrix.astype(np.float32)
        }
        return obs, reward, terminated, truncated, {}

        return self.adj_matrix.flatten(), reward, terminated, truncated, {}

def mask_fn(env: gym.Env) -> np.ndarray:
    return env.valid_action_mask()

def create_xml(filename, rowNum, columnNum, tree, xmlOther=None):
    # ※ rowNum, columnNum, tree, xmlOther, file_name 等は事前に定義されていると想定します
    file_name = os.path.join(tree_xmlpath, filename)
    xml = "<hex>\n"
    # f-string（フォーマット文字列）を使って変数を直接埋め込むとスッキリ書けます
    xml += f'<conf r="{rowNum}" c="{columnNum}"/>\n'

    # Pythonではイテレータをそのままfor文で回せます
    for i in range(len(tree)):
        # Javaのメソッド名はキャメルケースですが、Pythonの慣習に合わせてスネークケースにしています
        # 実際のPythonクラスの実装に合わせて node.children や node.id に変更してください
        for j in tree[i]:
            xml += f'<seg in="{i+1}" out="{j+1}"/>\n'

    # Pythonでは `if xmlOther:` だけで「Noneではない、かつ空文字列ではない」を判定できます
    if xmlOther:
        xml += xmlOther
    else:
        #長い文字列はトリプルクォート（"""）で囲むと複数行をそのまま書けます
        xml += """    <ref mode="CSV" name="R32.CSV"/>
    <air mode="CSV" name="AIR.CSV"/>
    <hx_type type="2" />
    <correlation type="1" />
    <param type="double" name="tube_D_o" val="0.00635"/>
    <param type="double" name="tube_D_i" val="0.00535"/>
    <param type="double" name="tube_L" val="0.5"/>
    <param type="double" name="tube_T" val="0.0005"/>
    <param type="double" name="tube_Hspace" val="0.016"/>
    <param type="double" name="tube_Vspace" val="0.01905"/>
    <param type="double" name="tube_beta" val="0.0"/>
    <param type="double" name="tube_k" val="0.205"/>
    <param type="double" name="fin_FPM" val="0.0"/>
    <param type="double" name="fin_P" val="0.0"/>
    <param type="double" name="fin_S" val="0.0012"/>
    <param type="double" name="fin_T" val="0.000115"/>
    <param type="double" name="fin_k" val="0.205"/>
    <param type="double" name="T_a_inlet" val="26.0"/>
    <param type="double" name="P_a_inlet" val="101.325"/>
    <param type="double" name="T_a_outlet" val="18.0"/>
    <param type="double" name="superheat" val="5.0"/>
    <param type="double" name="T_r_cond" val="35.0"/>
    <param type="double" name="subcool" val="5.0"/>
    <param type="double" name="evap_duty" val="3.0"/>
    </hex>"""
        #params = '<ref mode="CSV" name="R32.CSV"/>    <air mode="CSV" name="AIR.CSV"/>    <hx_type type="2" />    <correlation type="1" />    <param type="double" name="tube_D_o" val="0.01"/>   <param type="double" name="tube_D_i" val="0.0092"/> <param type="double" name="tube_L" val="0.5"/>  <param type="double" name="tube_T" val="0.0004"/>   <param type="double" name="tube_Hspace" val="0.0222"/>  <param type="double" name="tube_Vspace" val="0.0254"/>  <param type="double" name="tube_beta" val="0.0"/>   <param type="double" name="tube_k" val="0.205"/>    <param type="double" name="fin_FPM" val="0.0"/> <param type="double" name="fin_P" val="0.0"/>   <param type="double" name="fin_S" val="0.002"/> <param type="double" name="fin_T" val="0.0002"/>    <param type="double" name="fin_k" val="0.205"/> <param type="double" name="T_a_inlet" val="26.0"/>  <param type="double" name="P_a_inlet" val="101.325"/>   <param type="double" name="T_a_outlet" val="18.0"/> <param type="double" name="superheat" val="5.0"/>   <param type="double" name="T_r_cond" val="45.0"/>   <param type="double" name="subcool" val="5.0"/> <param type="double" name="evap_duty" val="4.0"/>\n'
        #xml+= params
    # ファイルの書き込みは `with` 構文を使うと、close() 処理を自動で行ってくれるため安全です
    with open(file_name, 'w', encoding='utf-8') as f:
        f.write(xml)




# # 2. .exeを実行
# stdin, stdout, stderr = ssh.exec_command('C:/path/to/program.exe')
# print(stdout.read().decode()) # 実行ログを確認

# # 3. 結果を回収
# sftp = ssh.open_sftp()
# sftp.get('C:/data/output.csv', 'result.csv')
# sftp.close()

# === 学習の実行 ===
if __name__ == "__main__":
    env = TreeBuilderEnv(row_num=12, column_num=3)

    env = ActionMasker(env, mask_fn)
    policy_kwargs = dict(
    features_extractor_class=GNNFeaturesExtractor,
    features_extractor_kwargs=dict(features_dim=128),
    )
    # ★ 通常の PPO ではなく、MaskablePPO を使用する
    model = MaskablePPO("MultiInputPolicy", env, policy_kwargs=policy_kwargs, verbose=1)
    #model = MaskablePPO("MlpPolicy", env, verbose=1)



    print("学習を開始します...")
    # 閉路を作らないルールを学習させるため、少し多めのステップ数を回します
    model.learn(total_timesteps=5000)
    print("学習が完了しました。")

    # === テスト実行 ===
    obs, info = env.reset()
    done = False
    print("\n推論（テスト）を開始します:")
    
    while not done:
        # ★ predict時に action_masks を渡すようになります
        action_masks = env.unwrapped.valid_action_mask()
        action, _states = model.predict(obs, action_masks=action_masks, deterministic=True)
        
        obs, reward, terminated, truncated, info = env.step(action)
        
        u = action // env.unwrapped.num_nodes
        v = action % env.unwrapped.num_nodes
        print(f"行動: 頂点 {u+1} と {v+1} を接続 | 獲得報酬: {reward}")
        
        done = terminated or truncated