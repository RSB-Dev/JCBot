package JCPackage;

import org.powerbot.script.*;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Script.Manifest(name="JewelryCrafting", description="Jewel Crafting", properties="client=4; author=MattyIce; topic=999;")

public class JCBot extends PollingScript<ClientContext> implements PaintListener{

    int itemId = 2357;
    int gemId = -1;
    String productMaterial = "Gold";
    String productType = "ring";
    int productHour = 0;
    int mouldId = 1592;
    int component1 = 446;
    int component2 = 7;
    int componentDynamic = 0;
    boolean withdrawA = true;
    boolean START = false;

    Tile furnaceTile = new Tile (3275,3186,0);
    Tile doorTile = new Tile (3280,3185,0);

    //GUI
    public class H extends JFrame implements ActionListener {

        JPanel p=new JPanel();
        JButton b = new JButton("Start");
        JLabel L = new JLabel("Material: ");
        JLabel L2 = new JLabel("Type: ");
        String CChoices[]={
                "Ring",
                "Amulet",
                "Necklace"
        };
        String CChoices2[]={
                "Gold",
                "Sapphire",
                "Emerald",
                "Ruby",
                "Diamond"
        };

        JComboBox C = new JComboBox(CChoices);
        JComboBox C2 = new JComboBox(CChoices2);

        public H(){
            super("JCBot");
            setSize(400,200);
            setResizable(true);
            setLocation(350,250);
            p.add(L);
            p.add(C2);
            p.add(L2);
            p.add(C);
            b.addActionListener(this);
            p.add(b);
            add(p);
            setVisible(true);
        }

        //GUI Event Listener
        public void actionPerformed(ActionEvent e) {
            if(C2.getSelectedIndex() == 1){productMaterial = "Sapphire"; gemId=1607; componentDynamic=1; withdrawA = false;}
            if(C2.getSelectedIndex() == 2){productMaterial = "Emerald"; gemId=1605; componentDynamic=2; withdrawA = false;}
            if(C2.getSelectedIndex() == 3){productMaterial = "Ruby"; gemId=1603; componentDynamic=3; withdrawA = false;}
            if(C2.getSelectedIndex() == 4){productMaterial = "Diamond"; gemId=1601; componentDynamic=4; withdrawA = false;}

            if(C.getSelectedIndex() == 1){productType = "amulet (u)"; mouldId = 1595; component1 = 446; component2 = 34;}
            if(C.getSelectedIndex() == 2){productType = "necklace"; mouldId = 1597; component1 = 446; component2 = 21;}
            START = true;
            System.out.println("Material:"+productMaterial+", Product Type:"+productType+", GemId:"+gemId);
            dispose();
        }
    }

    //Dynamic Sleeps
    public int smallSleep() {
        return (int) (Math.random() * 1250 + 650);
    }

    public int mediumSleep() {
        return (int) (int) (Math.random() * 2450 + 1050);
    }

    public int longSleep() {
        return (int) (Math.random() * 4987 + 2350);
    }

    //Anti Ban Sleep
    public void AntiBanSleep() {
        System.out.println("Anti Ban -- Sleeping");
        Condition.sleep((int) (Math.random() * 39087 + 12350));
    }

    //Check Bank Supply Status
    public void SupplyCheck(int id, int id2) {
        Condition.sleep(200);
        if (ctx.bank.select().id(id).count() == 0 || ctx.bank.select().id(id2).count() == 0 && id2 != -1) {
            //If bank does not contain supplies -- Stop Script
            Condition.sleep(200);
            System.out.println("Out of Supplies -- Stopping Script");
            System.exit(0);
        }
        else {System.out.println("Supply check -- OK");}
    }

    //Move to Nearest Bank -- Open Bank
    public void GetBank() {
        Tile nearestBank = ctx.bank.nearest().tile();

        if (ctx.movement.energyLevel() > (int) (Math.random() * 19 + 51) && ctx.movement.running() == false) {
            ctx.movement.running(true);
            Condition.sleep(smallSleep());
        }

        while (ctx.bank.inViewport() == false){
            //move to nearestBank tile
            System.out.println("Moving to Nearest Bank");
            Condition.sleep(mediumSleep());
            ctx.movement.step(nearestBank);
        }

        if (ctx.bank.inViewport() == true && ctx.bank.opened() == false){
            System.out.println("Opening Bank");
            Condition.sleep(smallSleep());
            ctx.bank.open();
            Condition.sleep(smallSleep());
            if ((int) (Math.random() * 20 + 1) == 10) {
                AntiBanSleep();
            }
        }
    }

    @Override
    public void start() {
        System.out.println("Starting Jewellery Crafting Bot");
        new H();
    }

    @Override
    public void poll() {
        while(START == true) {
            if ((int) (Math.random() * 200 + 1) == 100) {
                AntiBanSleep();
            }

            //Check Inventory Mould Status
            boolean mouldStatus = false;
            if (ctx.inventory.select().id(mouldId).count() > 0) {
                //No mould -- set mould status to true
                mouldStatus = true;
            }

            //Check Inventory Supply Status
            if (ctx.inventory.select().id(itemId).count() == 0 && ctx.inventory.select().name(productMaterial+" "+productType).count() == 0 && ctx.inventory.select().id(gemId).count() == 0) {
                //No gold bars and no gold rings in inventory -- need to withdraw bars
                System.out.println("No supplies or jewelry in inventory -- Banking");
                GetBank();
                if (ctx.bank.opened() == true) {
                    SupplyCheck(itemId, gemId);
                }
                if (mouldStatus == false) {
                    if (ctx.bank.select().id(mouldId).count() > 0) {
                        ctx.bank.withdrawAmount(mouldId, 1);
                        mouldStatus = true;
                        Condition.sleep(smallSleep());
                    } else {
                        System.exit(0);
                    }
                }
                if(ctx.inventory.select().id(itemId).count() != 0 || ctx.inventory.select().id(gemId).count() != 0 && gemId != -1){
                    ctx.bank.deposit(itemId, Bank.Amount.ALL);
                    ctx.bank.deposit(gemId, Bank.Amount.ALL);
                }
                if(withdrawA == true) {
                    ctx.bank.withdrawAmount(itemId, Bank.Amount.ALL);
                    productHour = productHour+27;
                }
                else {
                    ctx.bank.withdrawAmount(itemId, 13);
                    ctx.bank.withdrawAmount(gemId,13);
                    productHour = productHour+13;
                }
                Condition.sleep(smallSleep());
                ctx.bank.close();
            }

            //Check if Gold Bars are Done Smelting
            if (ctx.inventory.select().id(itemId).count() == 0 && mouldStatus == true || ctx.inventory.select().id(gemId).count() == 0 && gemId != -1) {
                //Inventory contains no bars, but contains rings -- need to deposit
                System.out.println("Done Smelting -- Banking");
                GetBank();
                if (ctx.bank.opened() == true) {
                    SupplyCheck(itemId, gemId);
                }
                ctx.bank.deposit(productMaterial+" "+productType, Bank.Amount.ALL);
                Condition.sleep(smallSleep());
                if(ctx.inventory.select().id(itemId).count() != 0 || ctx.inventory.select().id(gemId).count() != 0 && gemId != -1){
                    ctx.bank.deposit(itemId, Bank.Amount.ALL);
                    ctx.bank.deposit(gemId, Bank.Amount.ALL);
                }
                if(withdrawA == true) {
                    ctx.bank.withdrawAmount(itemId, Bank.Amount.ALL);
                    productHour = productHour+27;
                }
                else {
                    ctx.bank.withdrawAmount(itemId, 13);
                    ctx.bank.withdrawAmount(gemId,13);
                    productHour = productHour+13;
                }
                Condition.sleep(smallSleep());
                ctx.bank.close();
            }

            //Check if only Gold Bars and Mould are inventory
            if (ctx.inventory.select().id(itemId).count() > 0 && mouldStatus == true) {
                //Gold Bars and Mould in Inventory -- Get Furnace and Smelt
                boolean furnaceReachable = ctx.movement.reachable(doorTile, furnaceTile);
                if (furnaceReachable == true && ctx.movement.distance(furnaceTile) > 3) {
                    ctx.movement.step(furnaceTile);
                    System.out.println("Moving to furnace");
                    Condition.sleep(mediumSleep());
                }
                if (furnaceReachable == false) {
                    Condition.sleep(mediumSleep());
                    ctx.movement.step(doorTile);
                    if (ctx.movement.distance(doorTile) < 2) {
                        System.out.println("Opening Door");
                        ctx.objects.select().name("Door").nearest().poll().click("Open");
                    }
                }
                Condition.sleep(smallSleep());
                if (furnaceReachable == true && ctx.movement.distance(furnaceTile) < 3 && ctx.players.local().animation() != 899) {
                    Condition.sleep(smallSleep());
                    if (ctx.players.local().animation() != 899 && ctx.inventory.select().id(itemId).count() > 0) {
                        ctx.objects.select().name("Furnace").poll().click("Smelt");
                        System.out.println("Smelting");
                        Condition.sleep(mediumSleep());
                        ctx.widgets.component(component1,component2+componentDynamic).click();
                        Condition.sleep(longSleep());
                    }
                }
            }
        }
    }

    //Graphical Overlay
    int initialCrafting = ctx.skills.experience(Constants.SKILLS_CRAFTING);
    double startTime = System.currentTimeMillis();

    @Override
    public void repaint(Graphics graphics) {
        int currentCrafting = ctx.skills.experience(Constants.SKILLS_CRAFTING);
        int craftingGained = currentCrafting - initialCrafting;
        double endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime)/1000;
        double totalTimeHours = totalTime/60/60;
        long time = (long)((endTime-startTime)/1000);
        String format = String.format("%%0%dd", 2);
        String seconds = String.format(format, time % 60);
        String minutes = String.format(format, (time % 3600) / 60);
        String hours = String.format(format, time / 3600);
        String elapsedTime =  hours + ":" + minutes + ":" + seconds;
        double craftingRateDouble = craftingGained/totalTimeHours;
        int craftingRate = (int)craftingRateDouble;
        int productPerHour = (int)(productHour/totalTimeHours);
        craftingGained = Math.round(craftingGained);
        craftingRate = Math.round(craftingRate);

        //Crafting xp gained, xp/hr, items/hr, runtime
        graphics.setColor(new Color(255, 184, 0, 228));
        graphics.fillRect(0, 0, 175, 125);
        graphics.setColor(new Color(0, 97, 255));
        graphics.setFont(new Font("Roboto", Font.BOLD, 15));
        graphics.drawRect(0, 0, 175, 125);
        graphics.drawString("JCBot v0.5", 7, 20);
        graphics.setFont(new Font("Roboto", Font.BOLD, 12));
        graphics.drawString("Runtime: "+elapsedTime, 7, 45);
        graphics.drawString("Crafting xp gained: "+craftingGained,7,70);
        graphics.drawString("Crafting xp/hr: "+craftingRate,7,95);
        graphics.drawString("Product/hr: "+productPerHour,7,120);
    }
}