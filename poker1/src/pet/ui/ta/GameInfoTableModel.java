package pet.ui.ta;

import java.util.*;

import pet.hp.util.PlayerGameInfo;
import pet.hp.util.PlayerInfo;

public class GameInfoTableModel extends MyTableModel<PlayerGameInfo> {
	
	private static final List<MyTableModelColumn<PlayerGameInfo,?>> cols = new ArrayList<MyTableModelColumn<PlayerGameInfo,?>>();
	
	static {
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>("Game", "Game description", String.class) {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.game.id;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>("Hands", "Number of hands", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>("H-Show%", "Percentage of hands reaching show down", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.showdown*100f) / o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>("H-Won%", "Percentage of hands won", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.handswon*100f) / o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>("H-WonShow%", "Percentage of hands reaching show down that won", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.handswonshow*100f) / o.handswon;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>("AmPip", "Amount put in pot", Integer.class) {
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
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>("AmWon", "Amount won", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.won;
			}
			@Override
			public Integer getPopValue(PlayerGameInfo o) {
				return o.won / o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>("AmTot", "Amount won minus amount put in pot", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.won - o.pip;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Integer>("Rake", "Rake for hands won", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.rake;
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>("AF-Count", "Times bet+raise/check+call", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.af(false);
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>("AF-Vol", "Amount bet+raise/call", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.af(true);
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>("Ch/F-C-R", "Check, Check-fold, check-call, check-raise count", String.class) {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.cx();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>("Ch/F-C-R%", "Check-fold, check-call, check-raise percentage", String.class) {
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
