package pet.ui.ta;

import java.util.*;

import pet.hp.info.*;

public class GameInfoTableModel extends MyTableModel<PlayerGameInfo> {
	
	public static final List<MyColumn<PlayerGameInfo>> allcols = new ArrayList<MyColumn<PlayerGameInfo>>();
	public static final List<MyColumn<PlayerGameInfo>> pcols = new ArrayList<MyColumn<PlayerGameInfo>>();
	public static final List<MyColumn<PlayerGameInfo>> gcols = new ArrayList<MyColumn<PlayerGameInfo>>();
	
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
			return (o.vpip * 100f) / o.hands;
		}
	};
	private static final MyColumn<PlayerGameInfo> showdownseen = new MyColumn<PlayerGameInfo>(Float.class, "SS%", "Percentage of hands reaching show down") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return o.ss();
		}
	};
	private static final MyColumn<PlayerGameInfo> handswon = new MyColumn<PlayerGameInfo>(Float.class, "HW%", "Percentage of hands won") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return o.hw();
		}
	};
	private static final MyColumn<PlayerGameInfo> showdownswon = new MyColumn<PlayerGameInfo>(Float.class, "SW%", "Percentage of hands reaching show down that won") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return o.sw();
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
	private static final MyColumn<PlayerGameInfo> amtot = new MyColumn<PlayerGameInfo>(Integer.class, "AmTot", "Amount won minus amount put in pot") {
		@Override
		public Integer getValue(PlayerGameInfo o) {
			return o.won - o.pip;
		}
	};
	private static final MyColumn<PlayerGameInfo> rake = new MyColumn<PlayerGameInfo>(Integer.class, "Rake", "Rake for hands won") {
		@Override
		public Integer getValue(PlayerGameInfo o) {
			return o.rake;
		}
	};
	private static final MyColumn<PlayerGameInfo> afcount = new MyColumn<PlayerGameInfo>(Float.class, "AF-Count", "Times bet+raise/check+call") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return o.afcount();
		}
	};
	private static final MyColumn<PlayerGameInfo> afvol = new MyColumn<PlayerGameInfo>(Float.class, "AF-Vol", "Amount bet+raise/call") {
		@Override
		public Float getValue(PlayerGameInfo o) {
			return o.afam();
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
	
	static {
		// can't do arrays.addAll without type warning
		allcols.add(gameid);
		allcols.add(hands);
		allcols.add(vpip);
		allcols.add(showdownseen);
		allcols.add(handswon);
		allcols.add(showdownswon);
		allcols.add(ampip);
		allcols.add(amwon);
		allcols.add(amtot);
		allcols.add(rake);
		allcols.add(afcount);
		allcols.add(afvol);
		allcols.add(cx);
		allcols.add(cxr);
		allcols.add(init);
		
		pcols.add(player);
		pcols.add(hands);
		pcols.add(vpip);
		pcols.add(showdownseen);
		pcols.add(handswon);
		pcols.add(showdownswon);
		pcols.add(afcount);
		pcols.add(afvol);
		
		pcols.add(gameid);
		pcols.add(hands);
		pcols.add(vpip);
		pcols.add(showdownseen);
		pcols.add(handswon);
		pcols.add(showdownswon);
		pcols.add(afcount);
		pcols.add(afvol);
	}
	
	private PlayerInfo population;
	
	public GameInfoTableModel(List<MyColumn<PlayerGameInfo>> cols) {
		super(allcols, cols);
	}
	
	public void setPopulation(PlayerInfo population) {
		this.population = population;
		
	}
	
	@Override
	public PlayerGameInfo getPopulation(PlayerGameInfo row) {
		return population.getGameInfo(row.game);
	}
	
}
