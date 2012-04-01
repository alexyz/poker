package pet.ui.ta;

import java.util.*;

import pet.hp.util.PlayerGameInfo;
import pet.hp.util.PlayerInfo;

public class GameInfoTableModel extends MyTableModel<PlayerGameInfo> {
	
	private static final List<MyTableModelColumn<PlayerGameInfo,?>> cols = new ArrayList<MyTableModelColumn<PlayerGameInfo,?>>();
	
	static {
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>(String.class, "Game", "Game description") {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.game.id;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>(Integer.class, "Hands", "Number of hands") {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "H-Show%", "Percentage of hands reaching show down") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.showdown*100f) / o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "H-Won%", "Percentage of hands won") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.handswon*100f) / o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "H-WonShow%", "Percentage of hands reaching show down that won") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.handswonshow*100f) / o.handswon;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>(Integer.class, "AmPip", "Amount put in pot") {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.pip;
			}
			@Override
			public Integer getPopValue(PlayerGameInfo o) {
				// FIXME produces massive underestimate
				return o.pip / o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>(Integer.class, "AmWon", "Amount won") {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.won;
			}
			@Override
			public Integer getPopValue(PlayerGameInfo o) {
				return o.won / o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>(Integer.class, "AmTot", "Amount won minus amount put in pot") {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.won - o.pip;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>(Integer.class, "Rake", "Rake for hands won") {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.rake;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "AF-Count", "Times bet+raise/check+call") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.af(false);
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "AF-Vol", "Amount bet+raise/call") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.af(true);
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>(String.class, "Ch/F-C-R", "Check, Check-fold, check-call, check-raise count") {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.cx();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>(String.class, "Ch/F-C-R%", "Check-fold, check-call, check-raise percentage") {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.cxr();
			}
		});
	}
	
	private PlayerInfo population;
	
	public GameInfoTableModel() {
		super(cols);
	}
	
	public void setPopulation(PlayerInfo population) {
		this.population = population;
		
	}
	
	@Override
	public PlayerGameInfo getPopulation(PlayerGameInfo row) {
		return population.getGameInfo(row.game);
	}
	
	
}
