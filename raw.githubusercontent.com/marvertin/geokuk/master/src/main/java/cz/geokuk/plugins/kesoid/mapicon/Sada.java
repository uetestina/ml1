package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.Insets;
import java.util.*;

import javax.swing.Icon;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.plugins.kesoid.Repaintanger;
import cz.geokuk.plugins.kesoid.genetika.*;
import cz.geokuk.util.pocitadla.*;

public class Sada {

	private static Pocitadlo pocitSklivcuZasah = new PocitadloRoste("Sklivce - zásah cache", "");
	private final Pocitadlo pocitSklivcu = new PocitadloMalo("Sklivce - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");

	List<SkloAplikant> skloAplikanti = new ArrayList<>();

	Map<Genotyp, Sklivec> cache = new HashMap<>();

	private final String name;

	private final Repaintanger repaintanger = new Repaintanger();

	private Set<Alela> pouziteAlely;
	private Set<Gen> pouziteGeny;
	private Icon icon;

	public Sada(final String name) {
		this.name = name;
	}

	public Insets getBigiestIconInsets() {
		return repaintanger.getInsets();
	}

	/**
	 * @return the icon
	 */
	public Icon getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}

	/**
	 * Vrátí všechny alely, které jsou použity a podle nichž se dá něco zobrazit.
	 */
	public Set<Alela> getPouziteAlely() {
		if (pouziteAlely == null) {
			final Set<Alela> alely = new HashSet<>();
			for (final SkloAplikant skloAplikant : skloAplikanti) {
				for (final Vrstva vrstva : skloAplikant.sklo.vrstvy) {
					alely.addAll(vrstva.getPouziteAlely());
				}
			}
			pouziteAlely = alely;
		}
		return pouziteAlely;
	}

	public Set<Gen> getPouziteGeny() {
		if (pouziteGeny == null) {
			final Set<Gen> geny = new HashSet<>();
			final Set<Alela> pouziteAlely = getPouziteAlely();
			for (final Alela alela : pouziteAlely) {
				geny.add(alela.getGen());
			}
			pouziteGeny = geny;
		}
		return pouziteGeny;
	}

	/**
	 * Vrací sklivec pro daný genotyp s tím, že se hrabe v keši, aby se jednak zvýšila rychnlost, druhak, aby se šetřila paměť
	 *
	 * @param genotyp
	 * @return
	 */
	public synchronized Sklivec getSklivec(final Genotyp genotypx) {
		final Genotyp genotyp = genotypx.zuzNaObrazkove(getPouziteAlely()); // aby se nekešovalo pro alely, ke kterým nic nemáme
		Sklivec sklivec = cache.get(genotyp);
		if (sklivec == null) {
			sklivec = new Sklivec();
			for (final SkloAplikant skloAplikant : skloAplikanti) {
				sklivec.imaganti.add(getRenderedImage(genotyp, skloAplikant));
			}
			cache.put(genotyp, sklivec);
			repaintanger.include(sklivec);
			pocitSklivcu.set(cache.size());
			// System.out.println("REPREPA: " + repaintanger);
		} else {
			pocitSklivcuZasah.inc();
		}
		return sklivec;
	}

	/**
	 * @return the skloAplikanti
	 */
	public List<SkloAplikant> getSkloAplikanti() {
		return Collections.unmodifiableList(skloAplikanti);
	}

	/**
	 * @param aImageIcon
	 */
	public void setIcon(final Icon icon) {
		this.icon = icon;
	}

	/**
	 * @param genotyp
	 * @param skloAplikant
	 * @return
	 */
	private Imagant getRenderedImage(final Genotyp genotyp, final SkloAplikant skloAplikant) {
		final Imagant imagant = skloAplikant.sklo.getRenderedImage(genotyp);
		return imagant;
	}



}
