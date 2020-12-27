import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jp.fit.jc.it.BorderView;
import jp.fit.jc.it.CombinedView;
import jp.fit.jc.it.DrawItem;
import jp.fit.jc.it.GroupView;
import jp.fit.jc.it.PPanel2;

public class Hakyukouka2 extends JFrame implements ActionListener{
	static String XMLFileName = "Hakyukouka.xml";
	static String mondai[],kotae[];
	static int gmap[][]; // グループ分けデータ

	JButton reset,check,next,end; // 操作ボタン
	HakyukoukaPanel2 panel;
	static int number=0; // 問題番号
	String[] s,t;
	Element doc; // XML ファイルの内容が入る。
	Element xMondai; // XML形式の問題(1問のみ)

	public Hakyukouka2(String xml,int n) {

		super(); // いつもの設定
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setLayout(new BorderLayout());

		doc = ReadDocument.read(xml); // XMLを全て読み出しておく
		number =n;

		// タイトルを取り出してセットする。
		NodeList nl = doc.getElementsByTagName("title");     // タイトルタグ部分を得る
		setTitle(nl.item(0).getFirstChild().getNodeValue()); // 中身を取り出しタイトルバーにセット

		// 最初の問題を取り出してセットする。
		xMondai = (Element) doc.getElementsByTagName("Mondai").item(number);
		mondai = getStrings(xMondai,"question"); // 問題を取り出す。
		kotae  = getStrings(xMondai,"answer");    // 答えを取り出す
		s = getStrings(xMondai,"heya"); // まず配列として取り出す。
		gmap = new int[s.length][]; // 2次元配列の領域(行部分)確保
		for(int i=0; i < s.length; i++) {
		    t = s[i].split(","); // s[i]を区切り","で分割
		    gmap[i] = new int[t.length];   // 2次元配列の領域(列部分)確保
		    for(int j=0; j < t.length; j++) {
		        gmap[i][j] = Integer.parseInt(t[j]); // データを整数化し格納
		    }
		}

		int w = mondai[number].length(); // 横幅
		int h = mondai.length;      // 行数
		setSize(w*50-(w-7)*17,h*42-(h-7)*9);
		panel = new HakyukoukaPanel2(w,h,this); // パネルを作って配置
		add(panel,BorderLayout.CENTER);
		panel.setGroupMap(gmap); // データをセット
		panel.setMondai(mondai); // 問題をセットする。
		panel.setKinsi(mondai); // 問題をセットする。



		// 操作ボタンを左パネルに貼り付ける。
		JPanel lp = new JPanel();
		lp.setLayout(new GridLayout(4,1)); // グリッドレイアウトで6個のボタンまで

		reset = new JButton("リセット"); // ボタンを作る
		reset.addActionListener(this);
		check = new JButton("チェック");
		check.addActionListener(this);
		next = new JButton("次の問題");
		next.addActionListener(this);
		end = new JButton("終了");
		end.addActionListener(this);

		lp.add(reset);		// ボタンを配置する。
		lp.add(check);
		lp.add(next);
		lp.add(end);

		add(lp,BorderLayout.WEST); // 左パネルを左に配置
    }
	// メインルーチン
	public static void main(String args[]) {
		Hakyukouka2 frame = new Hakyukouka2(XMLFileName,number);
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		JButton btn = (JButton)e.getSource(); // ボタンを得る
		if( btn == reset ) { // リセットボタンかチェックする。
			int ret = JOptionPane.showConfirmDialog(null,
			"盤面を初期化します。", "リセット", JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.ERROR_MESSAGE);
			if(ret==JOptionPane.OK_OPTION) {
				panel.setGroupMap(gmap); // データをセット
				panel.setMondai(mondai);
				repaint();
			}
		} else if( btn == check) { // チェックボタンか
			String[] chare = panel.getCharaenge();
			if(checkAnswer(kotae,chare)) {   // 正解
				int ret = JOptionPane.showConfirmDialog(null,
					"おめでとう！正解です。次の問題へ移ります。", "チェック", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
				if(ret==JOptionPane.OK_OPTION) {
					goNext(); // 了解を選択した時の処
					} else {
						return; // 取り消しを選択した時の処理
					}
				} else {
					JOptionPane.showMessageDialog(null, "残念！どこか間違っています。",
		        "チェック", JOptionPane.INFORMATION_MESSAGE);
					}
			} else if(btn == next) { // 次の問題
				int ret = JOptionPane.showConfirmDialog(null,
				"次の問題に移ります。", "次の問題", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE);
				if(ret==JOptionPane.OK_OPTION) {
					goNext(); // 次の問題に移る。
					}
				} else if(btn == end) { // 終了ボタン：確認を入れるべき
					int ret = JOptionPane.showConfirmDialog(null,
				"終了します。", "終了", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE);
					if(ret==JOptionPane.OK_OPTION) {
						System.exit(0);
						}
					}
		}
	// 次の問題へ移る。メソッド化した。
	void goNext() {
		NodeList mn = doc.getElementsByTagName("Mondai"); // 全ての問題を取り出しておく
		number++;                       // 問題番号を増やす
		if(number >= mn.getLength()) {  // 最後の問題を通り過ぎたら最初に戻す。number = 0;
			number = 0;
		}
		dispose(); // 次の問題に移る
    	Hakyukouka2 frame = new Hakyukouka2(XMLFileName,number);
		frame.setVisible(true);
    }

	boolean checkAnswer(String[] a, String[] b) {
		if(a.length != b.length) { // そもそも大きさが違う
			return false;
			}
		for(int i = 0; i < a.length; i++) { // １行ごとに比較する
			if(! a[i].equals(b[i])) {
				return false;               // 違ったら、偽を返す
			}
		}
		return true;    // 全ての行が一致した。
	}

    // 問題や答えの文字列の並びを得る（答え合わせページに詳細）
    String[] getStrings(Element mondai,String tag) {
	    String s[];
	    NodeList nl = mondai.getElementsByTagName(tag);
	    s = new String[nl.getLength()];
	    for(int i = 0; i < nl.getLength() ;i++) {
		    try {
		        s[i] = nl.item(i).getFirstChild().getNodeValue();
		     } catch(Exception e) {
			     s[i] = "";
		    }
	    }
	    return s;
    }
}

class HakyukoukaPanel2 extends PPanel2 implements MouseListener {
	GroupView gview; // グループで分割するビュー
	CombinedView cview; // 複合ビュー、あとでアクセスするのでフィールドにしておく
	CombinedView iview; // 初期値のあるビュー：変更不可(コンストラクタで初期化)
	String rchars;
	Element xMondai; // XML形式の問題(1問のみ)

	boolean isAvailable(Point p) {
	    if(p == null) return false; // ユニット外、入力は不可
	    Character c = iview.getCharacter(p.x,p.y); // ※初期バージョンではこのメソッドが無い。
	    iview.setDefaultNullChar('□');
	    if(c == '\0' || c == iview.getDefaultNullChar()) return true;   // 初期値なし：入力可
	    return false; // 塗られているので入力不可
	}

	class DrawCharacter extends DrawItem {
    	Color fgColor; // 文字色
		public DrawCharacter(PPanel2 p,Color fg) {
			super(p);
			fgColor = fg;
		}
		public void draw(Graphics g, int px, int py, char ch) {
			cview.setFontColor(fgColor); // 色をセットし
			cview.drawChar(g, px, py, ch);
		}
	}

	HakyukoukaPanel2(int x, int y, JFrame f) {
		super(x,y,f);
		gview = new GroupView(this);
		cview = new CombinedView(this);
		iview = new CombinedView(this);
		addPView(gview);
		addPView(cview);
		addPView(iview);
		addPView(new BorderView(this, BorderView.FULL_BORDER,Color.black));
		addMouseListener(this); // マウスリスナ装着
		cview.setRotateChars("□１２３４５"); // 文字の変更順序をセット

		DrawItem bl = new DrawCharacter(this,Color.black); // 反転文字クラスのインスタンスを作る
		DrawItem gr = new DrawCharacter(this,Color.gray); // 反転文字クラスのインスタンスを作る
		cview.setDrawItem('１', bl);
		cview.setDrawItem('２', bl);
		cview.setDrawItem('３', bl);
		cview.setDrawItem('４', bl);
		cview.setDrawItem('５', bl);
		iview.setDrawItem('１', gr);
		iview.setDrawItem('２', gr);
		iview.setDrawItem('３', gr);
		iview.setDrawItem('４', gr);
		iview.setDrawItem('５', gr);
	}

	void setGroupMap(int[][] g) {
		gview.setGroupMap(g);
	}

	// 問題をセットする。
	void setMondai(String[] m) {
	   	cview.setCharByString(m);
	}

	void setKinsi(String[] m) {
	   	iview.setCharByString(m);
	}

    // チャレンジ文字列を得る。
    String[] getCharaenge() {
    	return cview.getCharenge();
    }

    public void mouseClicked(MouseEvent e) {
		Point p = getUnit(e);
		if(isAvailable(p)) {
			cview.rotateCharacter(p,false); // 文字を変更する
			frame.repaint();
	    }
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}