package teneo.MenuExplorer.client;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import teneo.MenuExplorer.server.MenuAllergensLogic;
import teneo.MenuExplorer.shared.IMenu;

@Component
public class MenuExplorer implements IMenu {

    private final FMenu fmenu;

    @Autowired
    public MenuExplorer(FMenu fmenu) {
        this.fmenu = fmenu;
    }

    @Override
    public String getOrderTitle(List<Integer> order) {
        return fmenu.getOrderTitle(order);
    }

    @Override
    public void setNewMenuAllergens(String allergensFile) throws FileNotFoundException {
        fmenu.setNewMenuAllergens(allergensFile);
    }

    @Override
    public MenuAllergensLogic getMenuAllergens() {
        return fmenu.getMenuAllergens();
    }

    @Override
    public String search(String query) {
        return fmenu.search(query);
    }

    @Override
    public String searchTop10(String query) {
        return fmenu.searchTop10(query);
    }

    @Override
    public void addMultipleToOrder(List<Integer> ids, List<Integer> order) {
        fmenu.addMultipleToOrder(ids, order);
    }

    @Override
    public void addToOrder(int id, List<Integer> order) {
        fmenu.addToOrder(id, order);
    }

    @Override
    public Set<Integer> getAllSubIds(int rootId) {
        return fmenu.getAllSubIds(rootId);
    }

    @Override
    public List<List<Integer>> getCart() {
        return fmenu.getCart();
    }

    @Override
    public int getCartTotalPrice() {
        return fmenu.getCartTotalPrice();
    }

    @Override
    public String getDescriptionFromId(int id) {
        return fmenu.getDescriptionFromId(id);
    }

    @Override
    public int getOrderPrice(List<Integer> order) {
        return fmenu.getOrderPrice(order);
    }

    @Override
    public JsonObject getProductById(int id) {
        return fmenu.getProductById(id);
    }

    @Override
    public JsonObject getRefById(int id) {
        return fmenu.getRefById(id);
    }

    @Override
    public Set<Integer> getSubIdsUnder(int baseId, int cutoffId) {
        return fmenu.getSubIdsUnder(baseId, cutoffId);
    }

    @Override
    public String getTitleForId(int id) {
        return fmenu.getTitleForId(id);
    }

    @Override
    public String printCart() {
        return fmenu.printCart();
    }

    @Override
    public String printOrder(List<Integer> order) {
        return fmenu.printOrder(order);
    }

    @Override
    public String printOrderOptions(List<Integer> order) {
        return fmenu.printOrderOptions(order);
    }

    @Override
    public void removeFromOrder(int id, List<Integer> order) {
        fmenu.removeFromOrder(id, order);
    }

    @Override
    public void removeMultipleFromOrder(List<Integer> ids, List<Integer> order) {
        fmenu.removeMultipleFromOrder(ids, order);
    }

    @Override
    public void removeOrder(List<Integer> order) {
        fmenu.removeOrder(order);
    }

    @Override
    public List<Integer> startOrder(int rootId) {
        return fmenu.startOrder(rootId);
    }
}