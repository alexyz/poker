package pet.ui.table;

import java.util.*;

import pet.eq.MathsUtil;
import pet.hp.info.*;

public class GameInfoTableModel extends MyTableModel<PlayerGameInfo> {
	
	public static final List<MyColumn<PlayerGameInfo>> allCols = new ArrayList<>();
	
	/** columns for displaying many games for one player */
	public static final List<MyColumn<PlayerGameInfo>> playerCols = new ArrayList<>();
	
	/** columns for displaying the one game for many players */
	public static final List<MyColumn<PlayerGameInfo>> gameCols = new ArrayList<>();
	
	private static final MyColumn<PlayerGameInfo> player = new MyColumn<PlayerGameInfo>(String.class, "Player", "Player Name") {
		@Override
		public String getValue(PlayerGameInfo o) {
			return o.player.name;
		}
	};
	
	private static final MyColumn<PlayerGameInfo> gameid = new MyColumn<PlayerGameInfo>(String.class, "Game", "Game identifier") {
		@Override
		public String getValue(PlayerGameInfo o) {
			return o.game.id;
		}
	};
	
	private static final MyColumn<PlayerGameInfo> hands = new MyColumn<PlayerGameInfo>(Integer.class, "Hands", "Number of hands") {
		@Override
		public Integer getValue(PlayerGameInfo o) {
			return o.hands;
		}
	};
	
	private static final MyColumn<PlayerGameInfo> vpip = new MyColumn<PlayerGameInfo>(Float.class, "VP%", "Percentage of hands VPIP") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return MathsUtil.trunc(o.vpip());
		}
	};
	
	private static final MyColumn<PlayerGameInfo> showdownseen = new MyColumn<PlayerGameInfo>(Float.class, "SS%", "Percentage of hands reaching show down") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return MathsUtil.trunc(o.ss());
		}
	};
	
	private static final MyColumn<PlayerGameInfo> handswon = new MyColumn<PlayerGameInfo>(Float.class, "HW%", "Percentage of hands won") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return MathsUtil.trunc(o.hw());
		}
	};
	
	private static final MyColumn<PlayerGameInfo> showdownswon = new MyColumn<PlayerGameInfo>(Float.class, "SW%", "Percentage of hands reaching show down that won") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return MathsUtil.trunc(o.sw());
		}
	};
	
	private static final MyColumn<PlayerGameInfo> ampip = new MyColumn<PlayerGameInfo>(Integer.class, "AmPip", "Amount put in pot") {
		@Override
		public Integer getValue(PlayerGameInfo o) {
			return o.pip;
		}
		@Override
		public Integer getPopValue(PlayerGameInfo o) {
			// FIXME produces massive underestimate
			return o.pip / o.hands;
		}
	};
	
	private static final MyColumn<PlayerGameInfo> amwon = new MyColumn<PlayerGameInfo>(Integer.class, "AmWon", "Amount won") {
		@Override
		public Integer getValue(PlayerGameInfo o) {
			return o.won;
		}
		@Override
		public Integer getPopValue(PlayerGameInfo o) {
			return o.won / o.hands;
		}
	};
	
	private static final MyColumn<PlayerGameInfo> amtot = new MyColumn<PlayerGameInfo>(Integer.class, "Am", "Amount won minus amount put in pot") {
		@Override
		public Integer getValue(PlayerGameInfo o) {
			return o.am();
		}
	};
	
	private static final MyColumn<PlayerGameInfo> amph = new MyColumn<PlayerGameInfo>(Float.class, "Am/H", "Amount won per hand") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return MathsUtil.trunc(o.amph());
		}
	};
	
	private static final MyColumn<PlayerGameInfo> rake = new MyColumn<PlayerGameInfo>(Integer.class, "Rake", "Rake for hands won") {
		@Override
		public Integer getValue(PlayerGameInfo o) {
			return o.rake;
		}
	};
	
	private static final MyColumn<PlayerGameInfo> af = new MyColumn<PlayerGameInfo>(Float.class, "AF", "Times bet+raise/call") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return o.af();
		}
	};
	
	private static final MyColumn<PlayerGameInfo> afch = new MyColumn<PlayerGameInfo>(Float.class, "AFch", "Times bet+raise/check+call") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return o.af(true);
		}
	};
	
	private static final MyColumn<PlayerGameInfo> cx = new MyColumn<PlayerGameInfo>(String.class, "ChFCR", "Check, Check-fold, check-call, check-raise count") {
		@Override
		public String getValue(PlayerGameInfo o) {
			return o.cx();
		}
	};
	
	private static final MyColumn<PlayerGameInfo> cxr = new MyColumn<PlayerGameInfo>(String.class, "ChFCR%", "Check-fold, check-call, check-raise percentage") {
		@Override
		public String getValue(PlayerGameInfo o) {
			return o.cxr();
		}
	};
	
	private static final MyColumn<PlayerGameInfo> init = new MyColumn<PlayerGameInfo>(String.class, "SI%", "Percentage of time initiative taken on each street") {
		@Override
		public String getValue(PlayerGameInfo o) {
			return o.isstr();
		}
	};
	
	public static final MyColumn<PlayerGameInfo> pfr = new MyColumn<PlayerGameInfo>(Float.class, "PFR%", "Percentage of hands raised preflop") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return MathsUtil.trunc(o.pfr());
		}
	};
	
	static {
		// can't do arrays.addAll without type warning
		allCols.add(gameid);
		allCols.add(hands);
		allCols.add(vpip);
		allCols.add(pfr);
		allCols.add(showdownseen);
		allCols.add(handswon);
		allCols.add(showdownswon);
		allCols.add(ampip);
		allCols.add(amwon);
		allCols.add(amtot);
		allCols.add(amph);
		allCols.add(rake);
		allCols.add(af);
		allCols.add(afch);
		allCols.add(cx);
		allCols.add(cxr);
		allCols.add(init);
		
		gameCols.add(player);
		gameCols.add(hands);
		gameCols.add(handswon);
		gameCols.add(vpip);
		gameCols.add(pfr);
		gameCols.add(showdownseen);
		gameCols.add(showdownswon);
		gameCols.add(af);
		gameCols.add(amtot);
		gameCols.add(amph);
		
		playerCols.add(gameid);
		playerCols.add(hands);
		playerCols.add(vpip);
		playerCols.add(showdownseen);
		playerCols.add(handswon);
		playerCols.add(showdownswon);
		playerCols.add(af);
		playerCols.add(amtot);
		playerCols.add(amph);
	}
	
	private PlayerInfo population;
	
	public GameInfoTableModel(List<MyColumn<PlayerGameInfo>> cols) {
		super(allCols, cols);
	}
	
	public void setPopulation(PlayerInfo population) {
		this.population = population;
		
	}
	
	@Override
	public PlayerGameInfo getPopulation(PlayerGameInfo row) {
		return population.getGameInfo(row.game.id);
	}
	
}
