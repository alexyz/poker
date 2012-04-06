package pet.ui.ta;

import java.util.*;

import pet.hp.info.*;

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
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "SS%", "Percentage of hands reaching show down") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.ss();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "HW%", "Percentage of hands won") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.hw();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "SW%", "Percentage of hands reaching show down that won") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.sw();
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
				return o.afcount();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,Float>(Float.class, "AF-Vol", "Amount bet+raise/call") {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.afam();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>(String.class, "ChFCR", "Check, Check-fold, check-call, check-raise count") {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.cx();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>(String.class, "ChFCR%", "Check-fold, check-call, check-raise percentage") {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.cxr();
			}
		});
		cols.add(new MyTableModelColumn<PlayerGameInfo,String>(String.class, "SI%", "Percentage of time initiative taken on each street") {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.isstr();
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
