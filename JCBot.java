package JCPackage;

import org.powerbot.script.*;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientContext;

import java.awt.*;

@Script.Manifest(name="JewelleryCrafting", description="Jewell Crafting", properties="client=4; author=MattyIce; topic=999;")

public class JCBot extends PollingScript<ClientContext> implements PaintListener {

    int itemId = 2357;
    int productId = 1635;
    int xpPerProduct = 15;
    int mouldId = 1592;
    Tile furnaceTile = new Tile (3275,3186,0);
    Tile doorTile = new Tile (3280,3185,0);

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
        System.out.println("Anti Ban Sleeping");
        Condition.sleep((int) (Math.random() * 39087 + 12350));
    }

    //Check Bank Supply Status
    public void SupplyCheck(int id) {
        if (ctx.bank.id(id).count() == 0) {
            //If bank does not contain supplies -- Stop Script
            Condition.sleep(200);
            System.out.println("Out of Gold Bars -- Stopping Script");
            System.exit(0);
        }
        else {
            System.out.println("Supply check -- OK");
        }
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
            System.out.println("opening Bank");
            Condition.sleep(smallSleep());
            ctx.bank.open();
            Condition.sleep(smallSleep());
            if ((int) (Math.random() * 15 + 1) == 10) {
                AntiBanSleep();
            }
        }
    }

    @Override
    public void start() {
        System.out.println("Starting Jewellery Crafting Bot");
    }

    @Override
    public void poll() {
        if ((int) (Math.random() * 200 + 1) == 100) {
            AntiBanSleep();
        }

        //Check Inventory Mould Status
        boolean mouldStatus = false;
        if (ctx.inventory.select().id(mouldId).count() > 0) {
            //No mould -- set mould status to true
            mouldStatus = true;
        }

        //Check Inventory Gold Bar Status
        if (ctx.inventory.select().id(itemId).count() == 0 && ctx.inventory.select().id(productId).count() == 0) {
            //No gold bars and no gold rings in inventory -- need to withdraw bars
            System.out.println("No golds bars or rings in inventory -- Banking");
            GetBank();
            if (ctx.bank.opened() == true) {
                SupplyCheck(itemId);
            }
            if (mouldStatus == false) {
                if(ctx.bank.select().id(mouldId).count() > 0) {
                    ctx.bank.withdrawAmount(mouldId, 1);
                    mouldStatus = true;
                    Condition.sleep(smallSleep());
                }
                else {System.exit(0);}
            }
            ctx.bank.withdrawAmount(itemId, Bank.Amount.ALL);
            Condition.sleep(smallSleep());
            ctx.bank.close();
        }

        //Check if Gold Bars are Done Smelting
        if (ctx.inventory.select().id(itemId).count() == 0 && ctx.inventory.select().id(productId).count() > 0){
           //Inventory contains no bars, but contains rings -- need to deposit
            System.out.println("Done Smelting -- Banking");
            GetBank();
            if (ctx.bank.opened() == true) {
                SupplyCheck(itemId);
            }
            ctx.bank.deposit(productId, Bank.Amount.ALL);
            Condition.sleep(smallSleep());
            ctx.bank.withdrawAmount(itemId, Bank.Amount.ALL);
            Condition.sleep(smallSleep());
            ctx.bank.close();
        }

        //Check if only Gold Bars and Mould are inventory
        if (ctx.inventory.select().id(itemId).count() > 0 && mouldStatus == true){
            //Gold Bars and Mould in Inventory -- Get Furnace and Smelt
            boolean furnaceReachable = ctx.movement.reachable(doorTile,furnaceTile);
            if (furnaceReachable == true && ctx.movement.distance(furnaceTile) > 3) {
                ctx.movement.step(furnaceTile);
                System.out.println("Moving to furnace");
                Condition.sleep(smallSleep());
            }
            if (furnaceReachable == false) {
                Condition.sleep(mediumSleep());
                ctx.movement.step(doorTile);
                if(ctx.movement.distance(doorTile) < 2) {
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
                    ctx.widgets.component(446, 7).click();
                    Condition.sleep(longSleep());
                }
            }
        }
    }

    int initialCrafting = ctx.skills.experience(Constants.SKILLS_CRAFTING);
    double startTime = System.currentTimeMillis();

    @Override
    public void repaint(Graphics graphics) {
        int currentCrafting = ctx.skills.experience(Constants.SKILLS_CRAFTING);
        int craftingGained = currentCrafting - initialCrafting;
        double endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime)/1000;
        double totalTimeHours = totalTime/60/60;
        double craftingRateDouble = craftingGained/totalTimeHours;
        int craftingRate = (int)craftingRateDouble;
        craftingGained = Math.round(craftingGained);
        craftingRate = Math.round(craftingRate);

        //Crafting xp gained, xp/hr, items/hr
        graphics.setColor(new Color(255, 184, 0, 228));
        graphics.fillRect(0, 0, 175, 125);

        graphics.setColor(new Color(0, 97, 255));
        graphics.setFont(new Font("Roboto", Font.BOLD, 15));
        graphics.drawRect(0, 0, 175, 125);
        graphics.drawString("JCBot v0.5", 7, 20);
        graphics.setFont(new Font("Roboto", Font.BOLD, 12));
        graphics.drawString("Runtime: "+totalTime+"s", 7, 45);
        graphics.drawString("Crafting xp gained: "+craftingGained,7,70);
        graphics.drawString("Crafting xp/hr: "+craftingRate,7,95);
        graphics.drawString("Product/hr: "+(craftingRate/xpPerProduct),7,120);
    }
}