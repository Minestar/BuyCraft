package de.minestar.buycraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import de.minestar.buycraft.shops.UserShop;
import de.minestar.buycraft.units.BlockVector;

public class ShopManager {

    private DatabaseManager databaseManager;
    private HashMap<String, UserShop> usershops;
    private boolean loadSucceeded = false;

    public ShopManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.usershops = new HashMap<String, UserShop>();
        this.loadUsershops();
    }

    public Sign getSignAnchor(Block block) {
        BlockVector position = new BlockVector(block.getLocation());
        if (this.isWallSign(block.getRelative(+1, 0, 0)) && block.getRelative(+1, 0, 0).getData() == 5) {
            Sign sign = (Sign) block.getRelative(+1, 0, 0).getState();
            if (this.isShop(sign.getLines(), position)) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(-1, 0, 0)) && block.getRelative(-1, 0, 0).getData() == 4) {
            Sign sign = (Sign) block.getRelative(-1, 0, 0).getState();
            if (this.isShop(sign.getLines(), position)) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(0, 0, +1)) && block.getRelative(0, 0, +1).getData() == 3) {
            Sign sign = (Sign) block.getRelative(0, 0, +1).getState();
            if (this.isShop(sign.getLines(), position)) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(0, 0, -1)) && block.getRelative(0, 0, -1).getData() == 2) {
            Sign sign = (Sign) block.getRelative(0, 0, -1).getState();
            if (this.isShop(sign.getLines(), position)) {
                return sign;
            }
        }
        return null;
    }
    public Sign getSignAnchor(List<Block> blockList) {
        Sign sign;
        for (Block block : blockList) {
            if ((sign = this.getSignAnchor(block)) != null) {
                return sign;
            }
        }
        return null;
    }

    /**
     * Check if a block is a chest
     * 
     * @param block
     * @return <b>true</b> if the block is a chest, otherwise <b>false</b>.
     */
    public boolean isChest(Block block) {
        return block.getTypeId() == Material.CHEST.getId();
    }

    /**
     * Check if a block is a wallsign
     * 
     * @param block
     * @return <b>true</b> if the block is a wallsign, otherwise <b>false</b>.
     */
    public boolean isWallSign(Block block) {
        return block.getTypeId() == Material.WALL_SIGN.getId();
    }

    /**
     * Check if a sign is a infinite-shopsign
     * 
     * @param lines
     * @return <b>true</b> if the block is a infinite-shopsign, otherwise
     *         <b>false</b>.
     */
    public boolean isInfiniteShop(String[] lines) {
        if (lines[0].equalsIgnoreCase("$SHOP$"))
            return true;
        return false;
    }

    /**
     * Check if a sign is a user-shopsign
     * 
     * @param lines
     * @return <b>true</b> if the block is a usershop, otherwise <b>false</b>.
     */
    public boolean isUserShop(BlockVector position) {
        return this.usershops.containsKey(position.toString());
    }

    /**
     * Get a usershop based on the position
     * 
     * @param position
     * @return the usershop
     */
    public UserShop getUserShop(BlockVector position) {
        return this.usershops.get(position.toString());
    }

    /**
     * Check if a sign is a shopsign
     * 
     * @param lines
     * @return <b>true</b> if the block is a shopsign, otherwise <b>false</b>.
     */
    private boolean isShop(String[] lines, BlockVector position) {
        return this.isInfiniteShop(lines) || this.isUserShop(position);
    }

    /**
     * Is at least one block a shopblock? Used for PlayerInteract. It will only
     * search for a sign and a chest.
     * 
     * @param block
     * @return <b>true</b> if the block is used for a shop, otherwise
     *         <b>false</b>.
     */
    public boolean isShopBlock(List<Block> blockList) {
        for (Block block : blockList) {
            // Is it a shopsign? (with a chest below it)
            if (this.isWallSign(block)) {
                Sign sign = (Sign) block.getState();
                if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
                    Block relative = block.getRelative(BlockFace.DOWN);
                    return this.isChest(relative);
                }
            }
            // Is it a chest? (with a shopsign above it)
            else if (this.isChest(block)) {
                Block relative = block.getRelative(BlockFace.UP);
                if (this.isWallSign(relative)) {
                    Sign sign = (Sign) relative.getState();
                    return this.isShop(sign.getLines(), new BlockVector(sign.getLocation()));
                }
            }
        }
        return false;
    }

    /**
     * Is a block a shopblock? Used for PlayerInteract. It will only search for
     * a sign and a chest.
     * 
     * @param block
     * @return <b>true</b> if the block is used for a shop, otherwise
     *         <b>false</b>.
     */
    public boolean isShopBlock(Block block) {
        // Is it a shopsign? (with a chest below it)
        if (this.isWallSign(block)) {
            Sign sign = (Sign) block.getState();
            if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
                Block relative = block.getRelative(BlockFace.DOWN);
                return this.isChest(relative);
            }
            return false;
        }
        // Is it a chest? (with a shopsign above it)
        if (this.isChest(block)) {
            Block relative = block.getRelative(BlockFace.UP);
            if (this.isWallSign(relative)) {
                Sign sign = (Sign) relative.getState();
                return this.isShop(sign.getLines(), new BlockVector(sign.getLocation()));
            }
            return false;
        }
        return false;
    }

    private void loadUsershops() {
        ArrayList<UserShop> shopList = this.databaseManager.loadUsershops();
        this.loadSucceeded = (shopList != null);
        if (this.loadSucceeded) {
            for (UserShop shop : shopList) {
                this.usershops.put(shop.getPosition().toString(), shop);
            }
        }
    }

    public UserShop addUsershop(BlockVector position) {
        UserShop newShop = this.databaseManager.addUsershop(position);
        if (newShop != null) {
            this.usershops.put(position.toString(), newShop);
        }
        return newShop;
    }

    public boolean removeUsershop(UserShop shop) {
        this.usershops.remove(shop.getPosition().toString());
        return this.databaseManager.removeUsershop(shop);
    }
}
